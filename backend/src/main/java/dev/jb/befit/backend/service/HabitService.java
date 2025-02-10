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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return habitRepository.findAllByUserAndDeletedFalse(user);
    }

    public Page<Habit> getHabitsByUser(User user, Pageable pageable) {
        return habitRepository.findAllByUserAndDeletedFalse(user, pageable);
    }

    public Optional<Habit> getHabitByUserAndId(User user, Long id) {
        return habitRepository.findHabitByUserAndId(user, id);
    }

    public List<Habit> getHabitsByUserAndTimeRange(User user, HabitTimeRange habitTimeRange) {
        return habitRepository.findAllByUserAndHabitTimeRangeAndDeletedFalse(user, habitTimeRange);
    }

    public Page<Habit> getHabitsByUserAndTimeRange(User user, HabitTimeRange habitTimeRange, Pageable pageable) {
        return habitRepository.findAllByUserAndHabitTimeRangeAndDeletedFalse(user, habitTimeRange, pageable);
    }

    public Page<Habit> searchHabit(User user, String filter, Pageable pageable) {
        return habitRepository.findAllByUserAndNameIgnoreCaseContainingAndDeletedFalseOrderByCreatedDesc(user, filter, pageable);
    }

    public HabitsByTimeRange getHabitsForToday(User user) {
        var date = LocalDate.now();
        var isEndOfWeek = date.getDayOfWeek() == DayOfWeek.SUNDAY;
        var isEndOfMonth = date.plusDays(1).getDayOfMonth() == 1;

        var daily = habitRepository.findAllByUserAndHabitTimeRangeAndDeletedFalse(user, HabitTimeRange.DAILY);
        List<Habit> weekly = List.of();
        List<Habit> monthly = List.of();

        if (isEndOfWeek) {
            weekly = habitRepository.findAllByUserAndHabitTimeRangeAndDeletedFalse(user, HabitTimeRange.WEEKLY);
        }

        if (isEndOfMonth) {
            monthly = habitRepository.findAllByUserAndHabitTimeRangeAndDeletedFalse(user, HabitTimeRange.MONTHLY);
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

    public void removeHabit(User user, Long habitId) {
        var habit = habitRepository.findHabitByUserAndId(user, habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        habit.setDeleted(true);
        habitRepository.save(habit);
    }
}
