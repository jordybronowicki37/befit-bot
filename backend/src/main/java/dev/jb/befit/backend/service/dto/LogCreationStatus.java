package dev.jb.befit.backend.service.dto;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.UserAchievement;

import java.util.List;

public record LogCreationStatus(
        ExerciseLog log,
        ExerciseLog lastLog,
        int amountOfLogs,
        ExerciseRecord record,
        Goal goal,
        List<UserAchievement> completedAchievements,
        long earnedXp,
        boolean firstLog,
        boolean newRecordReached,
        boolean goalReached
) {
}
