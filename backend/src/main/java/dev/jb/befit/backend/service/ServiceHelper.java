package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.models.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceHelper {
    public static Integer getLeaderboardPosition(User user, List<ExerciseRecord> records) {
        var recordsSorted = records.stream().sorted(Comparator.comparingInt(ExerciseRecord::getAmount)).toList();
        var userRecord = recordsSorted.stream().filter(r -> r.getUser().getId().equals(user.getId())).findFirst();
        if (userRecord.isEmpty()) return null;
        return recordsSorted.size() - recordsSorted.indexOf(userRecord.get());
    }

    public static Integer getCurrentPr(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return null;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        if (!allExerciseLogs.stream().allMatch(l -> l.getExerciseType() == exerciseType)) return null;
        var exerciseAmounts = allExerciseLogs.stream().map(ExerciseLog::getAmount).toList();
        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseAmounts.stream().max(Integer::compareTo).orElse(null);
        } else {
            return exerciseAmounts.stream().min(Integer::compareTo).orElse(null);
        }
    }

    public static boolean isPRImproved(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return false;
        if (allExerciseLogs.size() == 1) return true;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        if (!allExerciseLogs.stream().allMatch(l -> l.getExerciseType() == exerciseType)) return false;

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

    public static boolean isGoalReached(Goal goal, ExerciseLog exerciseLog) {
        if (!goal.getExerciseType().equals(exerciseLog.getExerciseType())) return false;
        if (goal.getExerciseType().getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseLog.getAmount() >= goal.getAmount();
        }
        else {
            return exerciseLog.getAmount() <= goal.getAmount();
        }
    }

    public static boolean isRecordImproved(ExerciseRecord exerciseRecord, ExerciseLog exerciseLog) {
        if (!exerciseRecord.getExerciseType().equals(exerciseLog.getExerciseType())) return false;

        if (exerciseRecord.getExerciseType().getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseLog.getAmount() > exerciseRecord.getAmount();
        }
        else {
            return exerciseLog.getAmount() < exerciseRecord.getAmount();
        }
    }
}
