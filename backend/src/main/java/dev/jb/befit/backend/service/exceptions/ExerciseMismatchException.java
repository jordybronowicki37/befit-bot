package dev.jb.befit.backend.service.exceptions;

public class ExerciseMismatchException extends MyException {
    public ExerciseMismatchException() {
        super("Exercise type mismatch");
    }
}
