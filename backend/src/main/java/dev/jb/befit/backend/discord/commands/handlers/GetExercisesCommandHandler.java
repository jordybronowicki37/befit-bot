package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import dev.jb.befit.backend.service.ServiceHelper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetExercisesCommandHandler extends DiscordChatInputInteractionEventListener {
    private static final int PAGE_SIZE = 5;

    private final ExerciseTypeService exerciseService;

    @Override
    public String getCommandNameFilter() {
        return "exercises view all";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.editReply(getExercisesEditSpec(0)).then();
    }

    public InteractionReplyEditSpec getExercisesEditSpec(int page) {
        var exercises = exerciseService.getPage(Pageable.ofSize(PAGE_SIZE).withPage(page));

        var embed = EmbedCreateSpec.builder()
                .title("All exercises")
                .fields(exercises
                        .stream()
                        .map(exercise -> {
                                    var records = exercise.getExerciseRecords();
                                    var descriptionBuilder = new StringBuilder();
                                    descriptionBuilder.append(String.format("Measurement: %s", exercise.getMeasurementType().getLongName()));
                                    descriptionBuilder.append(String.format("\nDirection: %s", exercise.getGoalDirection().name().toLowerCase()));
                                    descriptionBuilder.append(String.format("\nParticipants: %d", records.size()));
                                    if (!records.isEmpty()) {
                                        var firstPlace = ServiceHelper.sortLeaderboard(records).get(0);
                                        descriptionBuilder.append(String.format(
                                                "\n:first_place: %s %s - %s",
                                                CommandHandlerHelper.formatDouble(firstPlace.getAmount()),
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
        var previousButton = Button.secondary(String.format("exercises view all$%d", page-1), ReactionEmoji.unicode("⬅"));
        if (page <= 0) previousButton = previousButton.disabled();
        var nextButton = Button.secondary(String.format("exercises view all$%d", page+1), ReactionEmoji.unicode("➡"));
        if (page == exercises.getTotalPages()-1 || exercises.getTotalPages() == 0) nextButton = nextButton.disabled();

        var replyEditSpec = InteractionReplyEditSpec.builder().addEmbed(embed);
        if (!previousButton.isDisabled() || !nextButton.isDisabled()) {
            replyEditSpec.addComponent(ActionRow.of(previousButton, nextButton));
        }

        return replyEditSpec.build();
    }
}
