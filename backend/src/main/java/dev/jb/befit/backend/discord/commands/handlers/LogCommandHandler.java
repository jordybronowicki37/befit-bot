package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.MotivationalService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var exerciseName = event.getOption("exercise-name").orElseThrow().getValue().orElseThrow().asString();
        var exerciseAmount = Math.toIntExact(event.getOption("amount").orElseThrow().getValue().orElseThrow().asLong());
        var userId = event.getInteraction().getUser().getId();

        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.getActiveUserGoal(user, exerciseName);
        var exerciseLog = logService.create(user, exerciseName, exerciseAmount);
        var exerciseType = exerciseLog.getExerciseType();
        var reachedGoal = exerciseLog.getReachedGoal();
        var allExerciseLogs = logService.getAllByUserAndExerciseName(user, exerciseName);

        var workoutTitle = String.format("#%d %s - Log #%d", exerciseType.getId(), exerciseType.getName(), allExerciseLogs.size());
        var descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(String.format("Value: %d %s\n", exerciseLog.getAmount(), exerciseType.getMeasurementType().getShortName()));
        if (allExerciseLogs.size() >= 2) {
            var previousLog = allExerciseLogs.get(allExerciseLogs.size() - 2);
            descriptionBuilder.append(String.format("Last: %d %s\n", previousLog.getAmount(), exerciseType.getMeasurementType().getShortName()));
        }
        if (goal.isPresent() && reachedGoal == null) {
            descriptionBuilder.append(String.format("Goal: %d %s\n", goal.get().getAmount(), exerciseType.getMeasurementType().getShortName()));
        }
        var currentPr = getCurrentPr(allExerciseLogs);
        if (currentPr != null) {
            descriptionBuilder.append(String.format("Pr: %d %s", currentPr, exerciseType.getMeasurementType().getShortName()));
        }
        if (isNewPrReached(allExerciseLogs)) {
            descriptionBuilder.append("\n\n:rocket: NEW PR REACHED!");
        }
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

    private Integer getCurrentPr(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return null;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        var exerciseAmounts = allExerciseLogs.stream().map(ExerciseLog::getAmount).toList();
        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseAmounts.stream().max(Integer::compareTo).orElse(null);
        } else {
            return exerciseAmounts.stream().min(Integer::compareTo).orElse(null);
        }
    }

    private boolean isNewPrReached(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return false;
        if (allExerciseLogs.size() == 1) return true;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        var exerciseAmounts = new ArrayList<>(allExerciseLogs.stream().map(ExerciseLog::getAmount).toList());
        var lastValue = exerciseAmounts.remove(exerciseAmounts.size() - 1);

        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            var maxValue = exerciseAmounts.stream().max(Integer::compareTo);
            return maxValue.filter(integer -> integer < lastValue).isPresent();
        } else {
            var minValue = exerciseAmounts.stream().min(Integer::compareTo);
            return minValue.filter(integer -> integer > lastValue).isPresent();
        }
    }
}
