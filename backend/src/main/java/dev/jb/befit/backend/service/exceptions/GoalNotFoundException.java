package dev.jb.befit.backend.service.exceptions;

public class GoalNotFoundException extends MyException {
    public GoalNotFoundException(Long goalId) {
        super(String.format("Goal was not found with id: %s", goalId));
    }
}
