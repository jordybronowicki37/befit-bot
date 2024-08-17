package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandService;
import dev.jb.befit.backend.discord.listeners.DiscordEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        if (!event.getCommandName().equals("new-exercise-type")) return Mono.empty();

        var exerciseName = event.getOption("name").orElseThrow().getValue().orElseThrow().asString();
        var measurementType = event.getOption("measurement").orElseThrow().getValue().orElseThrow().asString();

        var exercise = exerciseService.create(exerciseName, measurementType);
        var embed = EmbedCreateSpec.builder()
                .title("Your new exercise")
                .addField(
                        String.format("#%d Exercise", exercise.getId()),
                        String.format("%s - %s", exercise.getName(), exercise.getMeasurementType()),
                        false)
                .color(Color.GREEN)
                .build();
        return event.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build());

        try {
            commandService.updateCommandsWithExerciseNameOptions();
        } catch (IOException e) {
            log.error("Failed to update commands with exercise name", e);
        }
    }
}
