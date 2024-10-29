package dev.jb.befit.backend.service.exceptions;

public class SessionNotFoundException extends MyException {
    public SessionNotFoundException() {
        super("Session was not found");
    }

    public SessionNotFoundException(Long id) {
        super(String.format("Session was not found with id: %d", id));
    }
}
