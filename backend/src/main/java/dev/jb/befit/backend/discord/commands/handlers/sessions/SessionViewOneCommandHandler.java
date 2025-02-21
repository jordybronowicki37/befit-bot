package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.UserAchievement;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.discord.registration.EmojiRegistrarService;
import dev.jb.befit.backend.service.ExerciseSessionService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.exceptions.SessionNotFoundException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionViewOneCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsViewOne;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var sessionId = CommandHandlerHelper.getOptionValueAsLong(subCommand, CommandConstants.AutoCompletePropSession);

        var user = userService.getOrCreateDiscordUser(userId);
        var session = exerciseSessionService.getByUserAndId(user, sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        var embed = getEmbed(session).build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }

    public static EmbedCreateSpec.Builder getEmbed(ExerciseSession session) {
        var logs = session.getExerciseLogs();
        var logsDescriptionBuilder = new StringBuilder();

        if (logs.isEmpty()) logsDescriptionBuilder.append("_You have not added any logs yet._");
        else {
            logs.stream().sorted(Comparator.comparing(ExerciseLog::getCreated)).forEach(log -> {
                addGeneralLogString(logsDescriptionBuilder, log);
                addAchievementsString(logsDescriptionBuilder, log.getAchievements().stream().map(UserAchievement::getAchievement).toList());
                addCongratulationsString(logsDescriptionBuilder, log);
                logsDescriptionBuilder.append("\n");
            });
        }

        var descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(String.format("Name: %s\n", session.getName()));
        descriptionBuilder.append(String.format("Started: %s\n", CommandHandlerHelper.discordFormatDateTime(session.getCreated())));
        descriptionBuilder.append(String.format("Status: %s\n", session.getStatus().getDisplayName()));
        descriptionBuilder.append(String.format("Total logs: %d\n", logs.size()));
        descriptionBuilder.append(String.format("### Logs\n%s", logsDescriptionBuilder));

        return EmbedCreateSpec.builder()
                .title(":notepad_spiral: Session")
                .description(descriptionBuilder.toString())
                .color(Color.GREEN);
    }

    private static void addGeneralLogString(StringBuilder stringBuilder, ExerciseLog log) {
        var exercise = log.getExerciseType();
        var measurement = exercise.getMeasurementType();
        var amount = CommandHandlerHelper.formatDouble(log.getAmount());
        var time = CommandHandlerHelper.discordFormatTime(log.getCreated());
        stringBuilder.append(String.format("**#%d %s**\nAmount: %s %s\nCreated: %s\nXp earned: %d\n", exercise.getId(), exercise.getName(), amount, measurement.getShortName(), time, log.getEarnedXp()));
    }

    private static void addAchievementsString(StringBuilder stringBuilder, List<Achievement> achievements) {
        if (achievements.isEmpty()) return;
        stringBuilder.append("Achievements:\n");
        for (var achievement : achievements) {
            stringBuilder.append(String.format("\u200B \u200B \u200B  <:%s:%s> %s \n", achievement.getDisplayName(), EmojiRegistrarService.getEmojiId(achievement, false).asString(), achievement.getTitle()));
        }
    }

    private static void addCongratulationsString(StringBuilder stringBuilder, ExerciseLog log) {
        var congratulations = new StringBuilder();
        // Add new pr reached congratulations
        if (log.isFirstLogOfExercise()) {
            congratulations.append("\u200B \u200B \u200B :sparkles: New exercise started!\n");
        }
        // Add new pr reached congratulations
        if (log.isPrImproved()) {
            congratulations.append("\u200B \u200B \u200B :rocket: New PR reached!\n");
        }
        // Add goal reached congratulations
        if (log.isGoalReached()) {
            congratulations.append("\u200B \u200B \u200B :chart_with_upwards_trend: Goal completed!\n");
        }
        // Add new level reached congratulations
        if (log.isLevelCompleted()) {
            congratulations.append("\u200B \u200B \u200B :star2: Level completed!\n");
        }
        if (!congratulations.isEmpty()) stringBuilder.append("Congratulations:\n").append(congratulations).append("\n");
    }
}
