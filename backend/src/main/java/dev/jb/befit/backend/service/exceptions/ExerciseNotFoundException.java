package dev.jb.befit.backend.service.exceptions;

public class ExerciseNotFoundException extends RuntimeException{
    public ExerciseNotFoundException(String message) {
        super(message);
    }
}
