package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.exceptions.ExerciseMismatchException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
}
