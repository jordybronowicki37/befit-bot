package dev.jb.befit.backend.service.dto;

import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitTimeRange;

import java.util.List;

public record HabitsByTimeRange(
        List<Habit> daily,
        List<Habit> weekly,
        List<Habit> monthly
) {
    public HabitsByTimeRange(List<Habit> habits) {
        this(
                habits.stream().filter(habit -> habit.getHabitTimeRange() == HabitTimeRange.DAILY).toList(),
                habits.stream().filter(habit -> habit.getHabitTimeRange() == HabitTimeRange.WEEKLY).toList(),
                habits.stream().filter(habit -> habit.getHabitTimeRange() == HabitTimeRange.MONTHLY).toList()
        );
    }
}
