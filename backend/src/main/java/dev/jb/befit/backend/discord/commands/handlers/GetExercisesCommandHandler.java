package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetExercisesCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseTypeService exerciseService;

    @Override
    public String getCommandNameFilter() {
        return "exercises view all";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var exercises = exerciseService.getAll();
        var embed = EmbedCreateSpec.builder()
                .title("All exercises")
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
