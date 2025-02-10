package dev.jb.befit.backend.service.exceptions;

public class NoHabitsFoundException extends MyException {
    public NoHabitsFoundException() {
        super("No habits were found");
    }
}
