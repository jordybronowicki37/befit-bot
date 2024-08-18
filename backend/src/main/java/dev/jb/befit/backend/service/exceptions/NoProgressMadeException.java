package dev.jb.befit.backend.service.exceptions;

import dev.jb.befit.backend.data.models.ExerciseType;

public class NoProgressMadeException extends MyException {
    public NoProgressMadeException(String message) {
        super(message);
    }

    public NoProgressMadeException(ExerciseType exerciseType) {
        super(String.format("You have no progress yet for exercise: #%d %s", exerciseType.getId(), exerciseType.getName()));
    }
}
