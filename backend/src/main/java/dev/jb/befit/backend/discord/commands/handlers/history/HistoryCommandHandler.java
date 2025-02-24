package dev.jb.befit.backend.discord.commands.handlers.history;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.UserAchievement;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.discord.registration.EmojiRegistrarService;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService exerciseLogService;
    private final ExerciseTypeService exerciseTypeService;
    private final UserService userService;
    private static final String itemSpacing = "\u200B \u200B \u200B ";

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHistory;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var exerciseName = CommandHandlerHelper.getOptionalOptionValue(event, CommandConstants.AutoCompletePropMyExerciseName, null);
        var userId = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(userId, 0, exerciseName)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page, String exerciseNameFilter) {
        var user = userService.getOrCreateDiscordUser(userId);
        Page<ExerciseLog> allLogs;
        if (exerciseNameFilter == null || exerciseNameFilter.equals("null")) {
            allLogs = exerciseLogService.getAllRecentByUser(user, Pageable.ofSize(CommandConstants.PageSize).withPage(page));
        } else {
            var exercise = exerciseTypeService.getByName(exerciseNameFilter).orElseThrow(() -> new ExerciseNotFoundException(exerciseNameFilter));
            allLogs = exerciseLogService.getAllRecentByUser(user, exercise.getId(), Pageable.ofSize(CommandConstants.PageSize).withPage(page));
        }

        var embed = EmbedCreateSpec.builder()
                .title("Log history")
                .fields(allLogs
                        .stream()
                        .map(log -> {
                            var exercise = log.getExerciseType();
                            var description = new StringBuilder();
                            addGeneralLogString(description, log);
                            addAchievementsString(description, log.getAchievements().stream().map(UserAchievement::getAchievement).toList());
                            addCongratulationsString(description, log);
                            description.append("\n");
                            return EmbedCreateFields.Field.of(
                                    String.format("#%d %s", exercise.getId(), exercise.getName()),
                                    String.format(description.toString()),
                                    false);
                        })
                        .toList())
                .color(Color.CYAN)
                .build();

        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, allLogs.getTotalPages(), String.format("%s$%s", getCommandNameFilter(), exerciseNameFilter));
        return InteractionReplyEditSpec.builder().addEmbed(embed).addComponent(paginationControls).build();
    }

    private static void addGeneralLogString(StringBuilder stringBuilder, ExerciseLog log) {
        var exercise = log.getExerciseType();
        var measurement = exercise.getMeasurementType();
        var amount = CommandHandlerHelper.formatDouble(log.getAmount());
        var time = CommandHandlerHelper.discordFormatTime(log.getCreated());
        stringBuilder.append(String.format("Amount: %s %s\nCreated: %s\nXp earned: %d\n", amount, measurement.getShortName(), time, log.getEarnedXp()));
    }

    private static void addAchievementsString(StringBuilder stringBuilder, List<Achievement> achievements) {
        if (achievements.isEmpty()) return;
        stringBuilder.append("Achievements:\n");
        for (var achievement : achievements) {
            stringBuilder.append(itemSpacing).append(String.format("<:%s:%s> %s \n", achievement.getDisplayName(), EmojiRegistrarService.getEmojiId(achievement, false).asString(), achievement.getTitle()));
        }
    }

    private static void addCongratulationsString(StringBuilder stringBuilder, ExerciseLog log) {
        var congratulations = new StringBuilder();
        // Add new exercise started congratulations
        if (log.isFirstLogOfExercise()) {
            congratulations.append(itemSpacing).append(":sparkles: New exercise started!\n");
        }
        // Add new pr reached congratulations
        if (log.isPrImproved()) {
            congratulations.append(itemSpacing).append(":rocket: New PR reached!\n");
        }
        // Add goal reached congratulations
        if (log.isGoalReached()) {
            congratulations.append(itemSpacing).append(":chart_with_upwards_trend: Goal completed!\n");
        }
        // Add new level reached congratulations
        if (log.isLevelCompleted()) {
            congratulations.append(itemSpacing).append(":star2: Level completed!\n");
        }
        if (!congratulations.isEmpty()) stringBuilder.append("Congratulations:\n").append(congratulations);
    }
}
