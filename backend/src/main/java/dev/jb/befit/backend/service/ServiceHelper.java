package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.service.exceptions.ExerciseMismatchException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceHelper {
    public static List<ExerciseRecord> sortLeaderboard(List<ExerciseRecord> records) {
        if (records.size() < 2) return records;
        var exercise = records.get(0).getExerciseType();
        if (!records.stream().allMatch(r -> r.getExerciseType().equals(exercise))) throw new ExerciseMismatchException();
        if (exercise.getGoalDirection().equals(GoalDirection.INCREASING)) {
            return records.stream().sorted((r1, r2) -> Double.compare(r2.getAmount(), r1.getAmount())).toList();
        }
        return records.stream().sorted(Comparator.comparingDouble(ExerciseRecord::getAmount)).toList();
    }

    public static Integer getLeaderboardPosition(User user, List<ExerciseRecord> records) {
        var recordsSorted = sortLeaderboard(records);
        var userRecord = recordsSorted.stream().filter(r -> r.getUser().getId().equals(user.getId())).findFirst();
        if (userRecord.isEmpty()) return null;
        return recordsSorted.indexOf(userRecord.get()) + 1;
    }

    public static Double getCurrentPr(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return null;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        if (!allExerciseLogs.stream().allMatch(l -> l.getExerciseType() == exerciseType)) throw new ExerciseMismatchException();
        var exerciseAmounts = allExerciseLogs.stream().map(ExerciseLog::getAmount).toList();
        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseAmounts.stream().max(Double::compareTo).orElse(null);
        } else {
            return exerciseAmounts.stream().min(Double::compareTo).orElse(null);
        }
    }

    public static boolean isPRImproved(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return false;
        if (allExerciseLogs.size() == 1) return true;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        if (!allExerciseLogs.stream().allMatch(l -> l.getExerciseType() == exerciseType)) throw new ExerciseMismatchException();

        var exerciseAmounts = new ArrayList<>(allExerciseLogs.stream().map(ExerciseLog::getAmount).toList());
        var lastValue = exerciseAmounts.remove(exerciseAmounts.size() - 1);

        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            var maxValue = exerciseAmounts.stream().max(Double::compareTo);
            return maxValue.filter(integer -> integer < lastValue).isPresent();
        } else {
            var minValue = exerciseAmounts.stream().min(Double::compareTo);
            return minValue.filter(integer -> integer > lastValue).isPresent();
        }
    }

    public static boolean isGoalReached(Goal goal, ExerciseLog exerciseLog) {
        if (!goal.getExerciseType().equals(exerciseLog.getExerciseType())) throw new ExerciseMismatchException();
        if (goal.getExerciseType().getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseLog.getAmount() >= goal.getAmount();
        }
        else {
            return exerciseLog.getAmount() <= goal.getAmount();
        }
    }

    public static boolean isRecordImproved(ExerciseRecord exerciseRecord, ExerciseLog exerciseLog) {
        if (!exerciseRecord.getExerciseType().equals(exerciseLog.getExerciseType())) throw new ExerciseMismatchException();

        if (exerciseRecord.getExerciseType().getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseLog.getAmount() > exerciseRecord.getAmount();
        }
        else {
            return exerciseLog.getAmount() < exerciseRecord.getAmount();
        }
    }
}
