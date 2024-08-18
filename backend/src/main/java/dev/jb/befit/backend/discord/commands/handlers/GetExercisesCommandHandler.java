package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetExercisesCommandHandler implements DiscordEventListener<ChatInputInteractionEvent> {
    private final ExerciseTypeService exerciseService;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        if (!CommandHandlerHelper.checkCommandName(event, "exercises view all")) return Mono.empty();

        event.deferReply().block();

        var exercises = exerciseService.getAll();
        var embed = EmbedCreateSpec.builder()
                .title("All Exercises")
                .fields(exercises
                        .stream()
                        .map(exercise -> EmbedCreateFields.Field.of(
                                String.format("#%d %s", exercise.getId(), exercise.getName()),
                                String.format("%s - %s",
                                        exercise.getMeasurementType().getLongName(),
                                        exercise.getGoalDirection().name().toLowerCase()
                                ),
                                false)
                        )
                        .toList())
                .color(Color.GREEN)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
