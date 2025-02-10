package dev.jb.befit.backend.service.exceptions;

public class UserNotFoundException extends MyException {
    public UserNotFoundException(Long userId) {
        super("User with id " + userId + " not found");
    }
}
