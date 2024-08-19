package dev.jb.befit.backend.service.exceptions;

import dev.jb.befit.backend.data.models.ExerciseType;

public class NotEnoughProgressException extends MyException {
    public NotEnoughProgressException(String message) {
        super(message);
    }

    public NotEnoughProgressException(ExerciseType exerciseType) {
        super(String.format("You need more progress for exercise: %s", exerciseType.getName()));
    }
}
