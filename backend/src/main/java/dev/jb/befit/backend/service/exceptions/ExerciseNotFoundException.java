package dev.jb.befit.backend.service.exceptions;

public class ExerciseNotFoundException extends MyException {
    public ExerciseNotFoundException(String exerciseName) {
        super(String.format("Exercise was not found of name: %s", exerciseName));
    }
}
