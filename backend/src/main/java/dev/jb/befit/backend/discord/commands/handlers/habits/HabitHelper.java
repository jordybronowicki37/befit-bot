package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HabitHelper {

    public static LocalDateTime getNextCheckListTimeForTimeRange(HabitTimeRange habitTimeRange) {
        var currentDate = LocalDateTime.now();
        // Reset seconds and minutes
        currentDate = currentDate.minusSeconds(currentDate.getSecond());
        currentDate = currentDate.minusMinutes(currentDate.getMinute());

        switch (habitTimeRange) {
            case DAILY:
                if (currentDate.getHour() > 19) {
                    currentDate = currentDate.plusDays(1);
                }
                break;
            case WEEKLY:
                var dayOfWeek = currentDate.getDayOfWeek().getValue();
                currentDate = currentDate.plusDays(7 - dayOfWeek);
                break;
            case MONTHLY:
                currentDate = currentDate.plusMonths(1);
                currentDate = currentDate.minusDays(currentDate.getDayOfMonth());
                break;
        }
        // Reset hours to 19 hours
        currentDate = currentDate.plusHours(19 - currentDate.getHour());
        return currentDate;
    }

    public static Long getAmountOfHabitCheckUps(Habit habit) {
        var start = habit.getCreated();
        var now = LocalDateTime.now();
        var amount = 0L;

        // If later than 19 hours, count from next day
        if (start.getHour() >= 19) {
            start = start.plusDays(1);
        }
        switch (habit.getHabitTimeRange()) {
            case DAILY:
                // Reset to 19 hours
                start = start.minusHours(start.getHour()).plusHours(19);
                start = start.minusMinutes(start.getMinute());
                start = start.minusSeconds(start.getSecond());

                while (start.isBefore(now)) {
                    amount += 1;
                    start = start.plusDays(1);
                }
                break;
            case WEEKLY:
                // Reset to last day of week 19 hours
                start = start.minusDays(start.getDayOfWeek().getValue()).plusDays(7);
                start = start.minusHours(start.getHour()).plusHours(19);
                start = start.minusMinutes(start.getMinute());
                start = start.minusSeconds(start.getSecond());

                while (start.isBefore(now)) {
                    amount += 1;
                    start = start.plusWeeks(1);
                }
                break;
            case MONTHLY:
                // Reset to last day of month 19 hours
                start = start.plusMonths(1).minusDays(start.getDayOfMonth());
                start = start.minusHours(start.getHour()).plusHours(19);
                start = start.minusMinutes(start.getMinute());
                start = start.minusSeconds(start.getSecond());

                while (start.isBefore(now)) {
                    amount += 1;
                    start = start.plusMonths(1);
                }
                break;
        }
        return amount;
    }
}
