package dev.jb.befit.backend.service.exceptions;

public class InvalidUserException extends MyException {
    public InvalidUserException() {
        super("You are not allowed to access or mutate this entity");
    }
}
