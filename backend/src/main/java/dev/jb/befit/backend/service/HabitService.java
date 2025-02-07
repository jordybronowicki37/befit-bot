package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.HabitLogRepository;
import dev.jb.befit.backend.data.HabitRepository;
import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitLog;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.dto.HabitsByTimeRange;
import dev.jb.befit.backend.service.exceptions.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    public List<Habit> getHabitsByUser(User user) {
        return habitRepository.findHabitByUser(user);
    }

    public Optional<Habit> getHabitByUserAndId(User user, Long id) {
        return habitRepository.findHabitByUserAndId(user, id);
    }

    public List<Habit> getHabitsByUserAndTimeRange(User user, HabitTimeRange habitTimeRange) {
        return habitRepository.findHabitByUserAndHabitTimeRange(user, habitTimeRange);
    }

    public HabitsByTimeRange getHabitsForToday(User user) {
        var date = LocalDate.now();
        var isEndOfWeek = date.getDayOfWeek() == DayOfWeek.SUNDAY;
        var isEndOfMonth = date.plusDays(1).getDayOfMonth() == 1;

        var daily = habitRepository.findHabitByUserAndHabitTimeRange(user, HabitTimeRange.DAILY);
        List<Habit> weekly = List.of();
        List<Habit> monthly = List.of();

        if (isEndOfWeek) {
            weekly = habitRepository.findHabitByUserAndHabitTimeRange(user, HabitTimeRange.WEEKLY);
        }

        if (isEndOfMonth) {
            monthly = habitRepository.findHabitByUserAndHabitTimeRange(user, HabitTimeRange.MONTHLY);
        }

        return new HabitsByTimeRange(daily, weekly, monthly);
    }

    public Habit createHabit(User user, String habitName, HabitTimeRange habitTimeRange) {
        var habit = new Habit(habitName, habitTimeRange, user);
        return habitRepository.save(habit);
    }

    public HabitLog flipHabitCompleted(User user, Long habitId, LocalDate date) {
        var habitLog = habitLogRepository.findByUserAndHabitIdAndLogDate(user, habitId, date);
        if (habitLog.isEmpty()) {
            var habit = getHabitByUserAndId(user, habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
            var newLog = new HabitLog(date, habit, user);
            return habitLogRepository.save(newLog);
        } else {
            habitLogRepository.delete(habitLog.get());
            return null;
        }
    }
}
