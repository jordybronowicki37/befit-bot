package dev.jb.befit.backend.service.dto;

import dev.jb.befit.backend.data.models.*;

import java.util.List;
import java.util.Optional;

public record LogCreationStatus(
        ExerciseLog log,
        ExerciseLog lastLog,
        int amountOfLogs,
        Optional<ExerciseSession> session,
        ExerciseRecord record,
        Goal goal,
        List<UserAchievement> completedAchievements,
        long earnedXp,
        boolean firstLog,
        boolean newRecordReached,
        boolean goalReached
) {
}
