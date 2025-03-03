package dev.jb.befit.backend.discord.commands.handlers.log;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.UserAchievement;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.discord.registration.EmojiRegistrarService;
import dev.jb.befit.backend.service.*;
import dev.jb.befit.backend.service.visuals.UserExperienceImageService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService logService;
    private final UserService userService;
    private final MotivationalService motivationalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandLog;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();

        var exerciseName = CommandHandlerHelper.getOptionValue(event, CommandConstants.AutoCompletePropExerciseName);
        var exerciseAmount = CommandHandlerHelper.getOptionValueAsDouble(event, "amount");

        var user = userService.getOrCreateDiscordUser(userId);
        var logCreationStatus = logService.create(user, exerciseName, exerciseAmount);
        var exerciseLog = logCreationStatus.log();
        var exerciseType = exerciseLog.getExerciseType();
        var goal = logCreationStatus.goal();
        var measurementName = exerciseType.getMeasurementType().getShortName();
        var userXp = user.getXp();
        var xpLevelData = UserExperienceService.getLevelData(userXp);

        // Construct embed
        var embed = EmbedCreateSpec.builder()
                .title(":muscle: Logged workout")
                .footer(motivationalService.getRandomPositiveReinforcement(), null)
                .color(Color.GREEN);

        // Construct main description
        {
            var workoutTitle = String.format("#%d %s - Log #%d", exerciseType.getId(), exerciseType.getName(), logCreationStatus.amountOfLogs());
            var descriptionBuilder = new StringBuilder();
            // Add session
            if (exerciseLog.getSession() != null) {
                descriptionBuilder.append(String.format("Session: `%s`\n", exerciseLog.getSession().getName()));
            }
            descriptionBuilder.append(String.format("Value: %s %s\n", CommandHandlerHelper.formatDouble(exerciseLog.getAmount()), measurementName));
            // Add last log value
            if (logCreationStatus.lastLog() != null) {
                var previousLog = logCreationStatus.lastLog();
                descriptionBuilder.append(String.format("Last: %s %s - %s\n", CommandHandlerHelper.formatDouble(previousLog.getAmount()), measurementName, CommandHandlerHelper.discordTimeAgoText(previousLog.getCreated())));
            }
            // Add goal if it is present and not yet reached
            if (goal != null && !exerciseLog.isGoalReached()) {
                descriptionBuilder.append(String.format("Goal: %s %s\n", CommandHandlerHelper.formatDouble(goal.getAmount()), measurementName));
            }
            // Add current pr
            var currentPr = logCreationStatus.record();
            if (currentPr != null) {
                descriptionBuilder.append(String.format("Pr: %s %s\n", CommandHandlerHelper.formatDouble(currentPr.getAmount()), measurementName));
            }
            var leaderboardPos = ServiceHelper.getLeaderboardPosition(user, exerciseType.getExerciseRecords());
            if (leaderboardPos != null)
                descriptionBuilder.append(String.format("Position: %s", CommandHandlerHelper.getLeaderboardValue(leaderboardPos)));
            embed.addField(workoutTitle, descriptionBuilder.toString(), false);
        }

        // Add user congratulations
        {
            var descriptionBuilder = new StringBuilder();
            // Add new exercise started congratulations
            if (exerciseLog.isFirstLogOfExercise()) {
                descriptionBuilder.append(":sparkles: New exercise started!\n");
            }
            // Add new pr reached congratulations
            if (exerciseLog.isPrImproved()) {
                descriptionBuilder.append(":rocket: New PR reached!\n");
            }
            // Add goal reached congratulations
            if (exerciseLog.isGoalReached()) {
                descriptionBuilder.append(":chart_with_upwards_trend: Goal completed!\n");
            }
            // Add new level reached congratulations
            if (exerciseLog.isLevelCompleted()) {
                descriptionBuilder.append(":star2: Level completed!\n");
            }
            if (!descriptionBuilder.isEmpty()) embed.addField("Congratulations", descriptionBuilder.toString(), false);
        }

        // Add user achievements
        if (!exerciseLog.getAchievements().isEmpty()) {
            var descriptionBuilder = new StringBuilder();

            exerciseLog.getAchievements().stream()
                    .map(UserAchievement::getAchievement)
                    .sorted(Comparator.comparing(Achievement::getTitle))
                    .forEach(a -> {
                        var emoji = EmojiRegistrarService.getEmojiId(a, false);
                        descriptionBuilder.append(String.format("<:%s:%s> %s\n*%s*\n\n", a.getDisplayName(), emoji.asString(), a.getTitle(), a.getDescription()));
                    });
            embed.addField("Completed achievements", descriptionBuilder.toString(), false);
        }

        // Add user xp field
        FileInputStream inputStream;
        {
            var levelDescription = String.format(":dizzy: Earned: %dxp - %dxp required for next level", exerciseLog.getEarnedXp(), xpLevelData.xpTopLevel());
            embed.addField("Experience", levelDescription, false);
            var userLevelXpBar = UserExperienceImageService.getXpLevelPicture(userXp);
            try {
                inputStream = new FileInputStream(userLevelXpBar);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            embed.image("attachment://level-xp-bar.png");
        }

        var undoButton = ActionRow.of(Button.secondary(String.format("%s$%d", CommandConstants.CommandLogUndo, exerciseLog.getId()), "Undo log"));
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed.build()).addFile("level-xp-bar.png", inputStream).addComponent(undoButton).build()).then();
    }
}
