package dev.jb.befit.backend.service.exceptions;

public class HabitNotFoundException extends MyException {
    public HabitNotFoundException(Long habitId) {
        super(String.format("Habit was not found with id: %s", habitId));
    }
}
