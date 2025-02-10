package dev.jb.befit.backend.discord.commands.handlers.history;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.ExerciseTypeService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService exerciseLogService;
    private final ExerciseTypeService exerciseTypeService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHistory;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var exerciseName = CommandHandlerHelper.getOptionalOptionValue(event, CommandConstants.AutoCompletePropExerciseName, null);
        var userId = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(userId, 0, exerciseName)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page, String exerciseNameFilter) {
        var user = userService.getOrCreateDiscordUser(userId);
        Page<ExerciseLog> allLogs;
        if (exerciseNameFilter == null) {
            allLogs = exerciseLogService.getAllRecentByUser(user, Pageable.ofSize(CommandConstants.PageSizeSmallItems).withPage(page));
        } else {
            var exercise = exerciseTypeService.getByName(exerciseNameFilter).orElseThrow(() -> new ExerciseNotFoundException(exerciseNameFilter));
            allLogs = exerciseLogService.getAllRecentByUser(user, exercise.getId(), Pageable.ofSize(CommandConstants.PageSizeSmallItems).withPage(page));
        }

        var embed = EmbedCreateSpec.builder()
                .title("Log history")
                .fields(allLogs
                        .stream()
                        .map(log -> {
                            var exercise = log.getExerciseType();
                            return EmbedCreateFields.Field.of(
                                    String.format("#%d %s", exercise.getId(), exercise.getName()),
                                    String.format("Created: %s\nValue: %s %s",
                                            CommandHandlerHelper.discordFormatDateTime(log.getCreated()),
                                            CommandHandlerHelper.formatDouble(log.getAmount()),
                                            exercise.getMeasurementType().getShortName()),
                                    false);
                        })
                        .toList())
                .color(Color.CYAN)
                .build();

        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, allLogs.getTotalPages(), String.format("%s$%s", getCommandNameFilter(), exerciseNameFilter));
        return InteractionReplyEditSpec.builder().addEmbed(embed).addComponent(paginationControls).build();
    }
}
