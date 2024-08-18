package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandService;
import dev.jb.befit.backend.discord.listeners.DiscordEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewExerciseCommandHandler implements DiscordEventListener<ChatInputInteractionEvent> {
    private final ExerciseTypeService exerciseService;
    private final CommandService commandService;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subcommand = event.getOption("create");
        if (!event.getCommandName().equals("exercises") || subcommand.isEmpty()) return Mono.empty();

        var exerciseName = subcommand.get().getOption("name").orElseThrow().getValue().orElseThrow().asString();
        var measurementType = subcommand.get().getOption("measurement").orElseThrow().getValue().orElseThrow().asString();

        event.deferReply().block();

        var exercise = exerciseService.create(exerciseName, measurementType);
        var embed = EmbedCreateSpec.builder()
                .title("Your new exercise")
                .addField(
                        String.format("#%d Exercise", exercise.getId()),
                        String.format("%s - %s", exercise.getName(), exercise.getMeasurementType()),
                        false)
                .color(Color.GREEN)
                .build();

        try {
            commandService.updateCommandsWithExerciseNameOptions();
        } catch (IOException e) {
            log.error("Failed to update commands with exercise name", e);
        }

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
