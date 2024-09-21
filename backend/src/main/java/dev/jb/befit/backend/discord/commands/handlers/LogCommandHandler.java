package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.*;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService logService;
    private final UserService userService;
    private final MotivationalService motivationalService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return "log";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();

        var exerciseName = CommandHandlerHelper.getOptionValue(event, "exercise-name");
        var exerciseAmount = CommandHandlerHelper.getOptionValueAsDouble(event, "amount");

        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.getActiveUserGoal(user, exerciseName);
        var exerciseLog = logService.create(user, exerciseName, exerciseAmount);
        var exerciseType = exerciseLog.getExerciseType();
        var reachedGoal = exerciseLog.getReachedGoal();
        var allExerciseLogs = logService.getAllByUserAndExerciseName(user, exerciseName);
        var measurementName = exerciseType.getMeasurementType().getShortName();

        // Construct message
        var workoutTitle = String.format("#%d %s - Log #%d", exerciseType.getId(), exerciseType.getName(), allExerciseLogs.size());
        var descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(String.format("Value: %s %s\n", CommandHandlerHelper.formatDouble(exerciseLog.getAmount()), measurementName));
        // Add last log value
        if (allExerciseLogs.size() >= 2) {
            var previousLog = allExerciseLogs.get(allExerciseLogs.size() - 2);
            descriptionBuilder.append(String.format("Last: %s %s - %s\n", CommandHandlerHelper.formatDouble(previousLog.getAmount()), measurementName, previousLog.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        }
        // Add goal if it is present and not yet reached
        if (goal.isPresent() && reachedGoal == null) {
            descriptionBuilder.append(String.format("Goal: %s %s\n", CommandHandlerHelper.formatDouble(goal.get().getAmount()), measurementName));
        }
        // Add current pr
        var currentPr = ServiceHelper.getCurrentPr(allExerciseLogs);
        if (currentPr != null) {
            descriptionBuilder.append(String.format("Pr: %s %s\n", CommandHandlerHelper.formatDouble(currentPr), measurementName));
        }
        var leaderboardPos = ServiceHelper.getLeaderboardPosition(user, exerciseType.getExerciseRecords());
        if (leaderboardPos != null) descriptionBuilder.append(String.format("Position: %s", CommandHandlerHelper.getLeaderboardValue(leaderboardPos)));

        // Add new pr reached congratulations
        if (ServiceHelper.isPRImproved(allExerciseLogs)) {
            descriptionBuilder.append("\n\n:rocket: NEW PR REACHED!");
        }
        // Add goal reached congratulations
        if (reachedGoal != null) {
            descriptionBuilder.append("\n\n:chart_with_upwards_trend: GOAL COMPLETED!");
        }

        var embed = EmbedCreateSpec.builder()
                .title(":muscle: Logged workout")
                .addField(workoutTitle, descriptionBuilder.toString(), false)
                .footer(motivationalService.getRandomPositiveReinforcement(), null)
                .color(Color.GREEN)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
