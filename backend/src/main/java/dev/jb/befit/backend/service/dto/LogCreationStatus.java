package dev.jb.befit.backend.service.dto;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.Goal;

public record LogCreationStatus(
        ExerciseLog log,
        ExerciseLog lastLog,
        int amountOfLogs,
        ExerciseRecord record,
        Goal goal
) {
}
