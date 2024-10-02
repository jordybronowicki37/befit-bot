package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.GoalRepository;
import dev.jb.befit.backend.data.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementsRulesHandler {
    private final UserAchievementService userAchievementService;
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final GoalRepository goalRepository;

    public List<UserAchievement> checkGoalCompletedAchievements(User user, Goal goal) {
        var achievements = new LinkedList<UserAchievement>();
        if (!goal.getStatus().equals(GoalStatus.COMPLETED)) return achievements;

        if (checkUserAchievementLocked(user, Achievement.GOAL_REACHED)) {
            achievements.add(userAchievementService.completeAchievement(user, Achievement.GOAL_REACHED));
        }

        if (checkUserAchievementLocked(user, Achievement.THE_RIGHT_MINDSET)) {
            var goals = goalRepository.findAllByUserAndCompletedAtAfterAndCompletedAtNotNull(user, LocalDateTime.now().minusDays(31));
            if (goals.size() >= 5) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.THE_RIGHT_MINDSET));
            }
        }

        return achievements;
    }

    public List<UserAchievement> checkLogCreationAchievements(User user, ExerciseLog exerciseLog) {
        var achievements = new LinkedList<UserAchievement>();
        var exerciseType = exerciseLog.getExerciseType();
        var exerciseIsIncreasing = exerciseType.getGoalDirection().equals(GoalDirection.INCREASING);

        if (checkUserAchievementLocked(user, Achievement.HEALTHY)) {
            achievements.add(userAchievementService.completeAchievement(user, Achievement.HEALTHY));
        }

        if (checkUserAchievementLocked(user, Achievement.HEART_MONITOR)) {
            if (exerciseIsOfType(exerciseType, MeasurementType.BPM)) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.HEART_MONITOR));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.CARDIO_ENTHUSIAST)) {
            if (exerciseIsOfCategory(exerciseType, MeasurementCategory.TIME) && exerciseIsIncreasing && exerciseLog.getAmount() >= 30) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.CARDIO_ENTHUSIAST));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.DONE_FOR_TODAY)) {
            var logs = exerciseLogRepository.findAllByUserAndCreatedAfter(user, LocalDateTime.now().minusDays(1));
            if (logs.size() >= 9) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.DONE_FOR_TODAY));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.FULL_WORKOUT)) {
            var logs = exerciseLogRepository.findAllByUserAndCreatedAfter(user, LocalDateTime.now().minusDays(1));
            var hasWeight = checkLogsForCategory(logs, MeasurementCategory.WEIGHT);
            var hasTime = checkLogsForCategory(logs, MeasurementCategory.TIME);
            var hasDistance = checkLogsForCategory(logs, MeasurementCategory.DISTANCE);
            if (hasWeight && hasTime && hasDistance) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.FULL_WORKOUT));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.KEEP_ON_STACKING)) {
            var logs = exerciseLogRepository.findAllByUserAndExerciseTypeId(user, exerciseType.getId());
            logs.add(exerciseLog);
            if (logs.size() >= 5) {
                var recentLogs = logs.stream().skip(logs.size() - 5).map(ExerciseLog::getAmount).toList();
                var previousValue = new AtomicReference<>(recentLogs.get(0));
                var isEverIncreasing = recentLogs.stream().skip(1).allMatch(r -> {
                    var prev = previousValue.get();
                    if (prev >= r) return false;
                    previousValue.set(r);
                    return true;
                });

                if (isEverIncreasing) {
                    achievements.add(userAchievementService.completeAchievement(user, Achievement.KEEP_ON_STACKING));
                }
            }
        }

        if (checkUserAchievementLocked(user, Achievement.LOVE_TO_LIFT)) {
            if (exerciseIsOfCategory(exerciseType, MeasurementCategory.WEIGHT) && exerciseIsIncreasing && exerciseLog.getAmount() >= 50) {
                var logs = exerciseLogRepository.findAllByUserAndCreatedAfter(user, LocalDateTime.now().minusDays(3));
                logs.add(exerciseLog);
                var groupedByDay = logs.stream()
                        .filter(l -> exerciseIsOfCategory(l.getExerciseType(), MeasurementCategory.WEIGHT))
                        .filter(l -> l.getExerciseType().getGoalDirection().equals(GoalDirection.INCREASING))
                        .filter(l -> l.getAmount() >= 50)
                        .collect(Collectors.groupingBy(entity -> entity.getCreated().toLocalDate()));

                if (groupedByDay.size() == 3) {
                    achievements.add(userAchievementService.completeAchievement(user, Achievement.LOVE_TO_LIFT));
                }
            }
        }

        if (checkUserAchievementLocked(user, Achievement.ON_A_ROLL)) {
            var logs = exerciseLogRepository.findAllByUserAndCreatedAfter(user, LocalDateTime.now().minusDays(4));
            logs.add(exerciseLog);
            var groupedByDay = logs.stream().collect(Collectors.groupingBy(entity -> entity.getCreated().toLocalDate()));
            if (groupedByDay.size() == 4) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.ON_A_ROLL));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.THINK_OF_HEALTH)) {
            if (exerciseIsOfType(exerciseType, MeasurementType.CALORIES) && exerciseIsIncreasing && exerciseLog.getAmount() >= 200) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.THINK_OF_HEALTH));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.HOME_GYM)) {
            var logs = exerciseLogRepository.findAllByUserAndCreatedAfter(user, LocalDateTime.now().minusDays(10));
            logs.add(exerciseLog);
            var groupedByDay = logs.stream().collect(Collectors.groupingBy(entity -> entity.getCreated().toLocalDate()));
            if (groupedByDay.size() == 10) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.HOME_GYM));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.LETS_GO_PLACES)) {
            if (exerciseIsOfType(exerciseType, MeasurementType.KM) && exerciseIsIncreasing && exerciseLog.getAmount() >= 20) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.LETS_GO_PLACES));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.SHOW_OFF)) {
            var exerciseRecords = exerciseType.getExerciseRecords()
                    .stream()
                    .sorted(Comparator.comparingDouble(ExerciseRecord::getAmount))
                    .toList();

            if (exerciseRecords.size() >= 6) {
                var firstPlaceUser = exerciseRecords.get(exerciseRecords.size() - 1).getUser();
                var showOffAchievement = userAchievementService.completeAchievement(firstPlaceUser, Achievement.SHOW_OFF);
                if (firstPlaceUser.equals(user)) {
                    achievements.add(showOffAchievement);
                }
            }
        }

        if (checkUserAchievementLocked(user, Achievement.THE_GOAT)) {
            var logs = exerciseLogRepository.findAllByUser(user);
            if (logs.size() >= 99) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.THE_GOAT));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.THE_HULK)) {
            if (exerciseIsOfType(exerciseType, MeasurementType.KG) && exerciseIsIncreasing && exerciseLog.getAmount() >= 100) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.THE_HULK));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.LIKE_A_MARATHON)) {
            if (exerciseIsOfType(exerciseType, MeasurementType.KM) && exerciseIsIncreasing && exerciseLog.getAmount() >= 42) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.LIKE_A_MARATHON));
            }
        }

        if (checkUserAchievementLocked(user, Achievement.SERIOUS_DEDICATION)) {
            var logs = exerciseLogRepository.findAllByUserAndCreatedAfter(user, LocalDateTime.now().minusDays(31));
            logs.add(exerciseLog);
            var groupedByDay = logs.stream().collect(Collectors.groupingBy(entity -> entity.getCreated().toLocalDate()));
            if (groupedByDay.size() == 31) {
                achievements.add(userAchievementService.completeAchievement(user, Achievement.SERIOUS_DEDICATION));
            }
        }

        return achievements;
    }

    private static boolean checkUserAchievementLocked(User user, Achievement achievement) {
        return user.getAchievements().stream().noneMatch(a -> a.getAchievement().equals(achievement));
    }

    private static boolean exerciseIsOfType(ExerciseType exerciseType, MeasurementType measurementType) {
        return exerciseType.getMeasurementType().equals(measurementType);
    }

    private static boolean exerciseIsOfCategory(ExerciseType exerciseType, MeasurementCategory measurementCategory) {
        return exerciseType.getMeasurementType().getCategory().equals(measurementCategory);
    }

    private static boolean checkLogsForCategory(List<ExerciseLog> exerciseLogs, MeasurementCategory measurementCategory) {
        return exerciseLogs.stream().anyMatch(b -> b.getExerciseType().getMeasurementType().getCategory().equals(measurementCategory));
    }
}
