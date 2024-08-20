package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import dev.jb.befit.backend.service.ServiceHelper;
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
                        .stream().limit(25)
                        .map(exercise -> {
                                    var records = exercise.getExerciseRecords();
                                    var descriptionBuilder = new StringBuilder();
                                    descriptionBuilder.append(String.format("Measurement: %s", exercise.getMeasurementType().getLongName()));
                                    descriptionBuilder.append(String.format("\nDirection: %s", exercise.getGoalDirection().name().toLowerCase()));
                                    descriptionBuilder.append(String.format("\nParticipants: %d", records.size()));
                                    if (!records.isEmpty()) {
                                        var firstPlace = ServiceHelper.sortLeaderboard(records).get(0);
                                        descriptionBuilder.append(String.format(
                                                "\n:first_place: %d %s - %s",
                                                firstPlace.getAmount(),
                                                exercise.getMeasurementType().getShortName(),
                                                CommandHandlerHelper.getUserStringValue(firstPlace.getUser())
                                        ));
                                    }
                                    return EmbedCreateFields.Field.of(
                                            String.format("#%d %s", exercise.getId(), exercise.getName()),
                                            descriptionBuilder.toString(),
                                            false);
                                }
                        )
                        .toList())
                .color(Color.GREEN)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
