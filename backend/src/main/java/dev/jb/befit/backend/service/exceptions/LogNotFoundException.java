package dev.jb.befit.backend.service.exceptions;

public class LogNotFoundException extends MyException {
    public LogNotFoundException(Long id) {
        super(String.format("Log was not found with id: %d", id));
    }
}
