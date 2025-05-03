package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.discord.commands.handlers.habits.HabitHelper;
import dev.jb.befit.backend.service.ExerciseLogService;
import discord4j.common.util.Snowflake;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("dummy-data")
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final UserAchievementsRepository userAchievementsRepository;
    private final ExerciseLogService exerciseLogService;
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final GoalRepository goalRepository;
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final Random random = new Random(1);

    @Value("${discord.dummy-user-id}")
    private String userId;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Started dummy-data initialization");
        var discordUser = new DiscordUser(Snowflake.of(userId));
        discordUser.setXp(810L);
        var webUser1 = new WebUser("test-user", "test@example.com", "12345678");
        var webUser2 = new WebUser("other-test-user", "other-test@example.com", "12345678");
        userRepository.saveAll(List.of(discordUser, webUser1, webUser2));
        userAchievementsRepository.save(new UserAchievement(Achievement.DONE_FOR_TODAY, discordUser));
        userAchievementsRepository.save(new UserAchievement(Achievement.CARDIO_ENTHUSIAST, webUser1));

        var habit1 = new Habit("Walked for 30 minutes", HabitTimeRange.DAILY, discordUser);
        habit1.setCreated(LocalDateTime.now().minusDays(60));
        addRandomLogsToHabit(habit1, discordUser);
        var habit2 = new Habit("Did some stretches", HabitTimeRange.DAILY, discordUser);
        habit2.setCreated(LocalDateTime.now().minusDays(60));
        addRandomLogsToHabit(habit2, discordUser);
        var habit3 = new Habit("Stayed out of the cookie jar", HabitTimeRange.DAILY, discordUser);
        habit3.setCreated(LocalDateTime.now().minusDays(50));
        addRandomLogsToHabit(habit3, discordUser);
        var habit4 = new Habit("Read a 'real' book for 30 minutes", HabitTimeRange.DAILY, discordUser);
        habit4.setCreated(LocalDateTime.now().minusDays(45));
        addRandomLogsToHabit(habit4, discordUser);
        var habit5 = new Habit("Went to the gym 3x", HabitTimeRange.WEEKLY, discordUser);
        habit5.setCreated(LocalDateTime.now().minusDays(40));
        addRandomLogsToHabit(habit5, discordUser);
        var habit6 = new Habit("Finished a book", HabitTimeRange.MONTHLY, discordUser);
        habit6.setCreated(LocalDateTime.now().minusDays(40));
        addRandomLogsToHabit(habit6, discordUser);
        var habit7 = new Habit("Had a good feeling about last week", HabitTimeRange.WEEKLY, discordUser);
        habit7.setCreated(LocalDateTime.now().minusDays(25));
        addRandomLogsToHabit(habit7, discordUser);
        var habit8 = new Habit("Said 'I love myself' in the mirror for 5x", HabitTimeRange.DAILY, discordUser);
        habit8.setCreated(LocalDateTime.now().minusDays(20));
        addRandomLogsToHabit(habit8, discordUser);

        var benchpress = new ExerciseType("Bench press", MeasurementType.KG, GoalDirection.INCREASING);
        var pullUp = new ExerciseType("Pull ups", MeasurementType.AMOUNT, GoalDirection.INCREASING);
        var running = new ExerciseType("Running 5km", MeasurementType.MINUTES, GoalDirection.DECREASING);
        var pecFly = new ExerciseType("Pec fly", MeasurementType.KG, GoalDirection.INCREASING);
        var shoulderPress = new ExerciseType("Shoulder press", MeasurementType.MINUTES, GoalDirection.INCREASING);
        var cycling = new ExerciseType("Cycling", MeasurementType.KM, GoalDirection.INCREASING);
        exerciseTypeRepository.saveAll(List.of(benchpress, pullUp, running, pecFly, shoulderPress, cycling));

        var benchLog1 = exerciseLogService.create(discordUser, benchpress.getName(), 30d).log();
        benchLog1.setCreated(LocalDateTime.now().minusDays(30));
        var benchLog2 = exerciseLogService.create(discordUser, benchpress.getName(), 35d).log();
        benchLog2.setCreated(LocalDateTime.now().minusDays(27));
        var benchLog3 = exerciseLogService.create(discordUser, benchpress.getName(), 40d).log();
        benchLog3.setCreated(LocalDateTime.now().minusDays(20));
        var benchLog4 = exerciseLogService.create(discordUser, benchpress.getName(), 45d).log();
        benchLog4.setCreated(LocalDateTime.now().minusDays(15));
        var benchLog5 = exerciseLogService.create(discordUser, benchpress.getName(), 50d).log();
        benchLog5.setCreated(LocalDateTime.now().minusDays(8));
        var benchLog6 = exerciseLogService.create(discordUser, benchpress.getName(), 55d).log();
        var benchLog7 = exerciseLogService.create(webUser1, benchpress.getName(), 35d).log();
        benchLog7.setCreated(LocalDateTime.now().minusDays(18));
        var benchLog8 = exerciseLogService.create(webUser1, benchpress.getName(), 40d).log();
        benchLog8.setCreated(LocalDateTime.now().minusDays(7));
        var benchLog9 = exerciseLogService.create(webUser1, benchpress.getName(), 45d).log();
        benchLog9.setCreated(LocalDateTime.now().minusDays(4));
        var benchLog10 = exerciseLogService.create(webUser2, benchpress.getName(), 55d).log();
        benchLog10.setCreated(LocalDateTime.now().minusDays(13));
        var benchLog11 = exerciseLogService.create(webUser2, benchpress.getName(), 50d).log();
        benchLog11.setCreated(LocalDateTime.now().minusDays(8));
        var benchLog12 = exerciseLogService.create(webUser2, benchpress.getName(), 60d).log();
        benchLog12.setCreated(LocalDateTime.now().minusDays(5));
        exerciseLogRepository.saveAll(List.of(benchLog1, benchLog2, benchLog3, benchLog4, benchLog5, benchLog6, benchLog7, benchLog8, benchLog9, benchLog10, benchLog11, benchLog12));

        var pullLog1 = exerciseLogService.create(discordUser, pullUp.getName(), 5d).log();
        pullLog1.setCreated(LocalDateTime.now().minusDays(3));
        var pullLog2 = exerciseLogService.create(webUser1, pullUp.getName(), 9d).log();
        pullLog2.setCreated(LocalDateTime.now().minusDays(3));
        var cyclingLog1 = exerciseLogService.create(discordUser, cycling.getName(), 10d).log();
        exerciseLogRepository.saveAll(List.of(pullLog1, pullLog2, cyclingLog1));

        var session = new ExerciseSession("My first session", discordUser);
        exerciseSessionRepository.save(session);
        benchLog6.setSession(session);
        pullLog1.setSession(session);
        cyclingLog1.setSession(session);
        exerciseLogRepository.saveAll(List.of(pullLog1, cyclingLog1, benchLog6));

        goalRepository.save(new Goal(60d, benchpress, discordUser));
        goalRepository.save(new Goal(10d, pullUp, discordUser));

        log.info("Finished dummy-data initialization");
    }

    private void addManyHabits(User user) {
        for (int i = 0; i < 10; i++) {
            var habit = new Habit("My daily habit " + (i + 1), HabitTimeRange.DAILY, user);
            habit.setCreated(LocalDateTime.now().minusDays(100));
            addRandomLogsToHabit(habit, user);
        }
        var weekDate = LocalDate.now();
        weekDate = weekDate.minusDays(weekDate.getDayOfWeek().getValue()).minusWeeks(52);
        for (int i = 0; i < 6; i++) {
            var habit = new Habit("My weekly habit " + (i + 1), HabitTimeRange.WEEKLY, user);
            habit.setCreated(weekDate.atStartOfDay());
            addRandomLogsToHabit(habit, user);
        }
        var monthDate = LocalDate.now();
        monthDate = monthDate.minusDays(monthDate.getDayOfWeek().getValue()).minusMonths(20);
        for (int i = 0; i < 4; i++) {
            var habit = new Habit("My monthly habit " + (i + 1), HabitTimeRange.MONTHLY, user);
            habit.setCreated(monthDate.atStartOfDay());
            addRandomLogsToHabit(habit, user);
        }
    }

    private void addRandomLogsToHabit(Habit habit, User user) {
        var amountOfCheckups = HabitHelper.getAmountOfHabitCheckUps(habit);
        habitRepository.save(habit);
        switch (habit.getHabitTimeRange()) {
            case DAILY: {
                for (int j = 0; j < amountOfCheckups; j++) {
                    var date = LocalDate.now().minusDays(amountOfCheckups).plusDays(j);
                    if (random.nextBoolean()) {
                        habitLogRepository.save(new HabitLog(date, habit, user));
                    }
                }
                break;
            }
            case WEEKLY: {
                var weekDate = LocalDate.now();
                weekDate = weekDate.minusDays(weekDate.getDayOfWeek().getValue());
                for (int j = 0; j < amountOfCheckups; j++) {
                    var date = weekDate.minusWeeks(amountOfCheckups).plusWeeks(j);
                    if (random.nextBoolean()) {
                        habitLogRepository.save(new HabitLog(date, habit, user));
                    }
                }
                break;
            }
            case MONTHLY: {
                var monthDate = LocalDate.now();
                monthDate = monthDate.minusDays(monthDate.getDayOfWeek().getValue());
                for (int j = 0; j < amountOfCheckups; j++) {
                    var date = monthDate.minusMonths(amountOfCheckups).plusMonths(j);
                    if (random.nextBoolean()) {
                        habitLogRepository.save(new HabitLog(date, habit, user));
                    }
                }
                break;
            }
        }
    }
}
