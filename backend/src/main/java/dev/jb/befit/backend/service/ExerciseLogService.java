package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.service.dto.LogCreationStatus;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseLogService {
    private final AchievementsRulesHandler achievementsRulesHandler;
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseTypeService exerciseTypeService;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final GoalService goalService;
    private final UserExperienceService userExperienceService;

    public List<ExerciseLog> getAllByUser(User user) {
        return exerciseLogRepository.findAllByUser(user);
    }

    public long countAllByUser(User user) {
        return exerciseLogRepository.countAllByUser(user);
    }

    public long countAmountOfExercisesByUser(User user) {
        return exerciseLogRepository.countDistinctExerciseTypeByUser(user);
    }

    public Page<ExerciseLog> getAllRecentByUser(User user, Pageable pageable) {
        return exerciseLogRepository.findAllByUserOrderByCreatedDesc(user, pageable);
    }

    public List<ExerciseLog> getAllByExerciseName(String exerciseName) {
        if (exerciseName.startsWith("#")) {
            var idString = exerciseName.substring(1);
            var id = Long.parseLong(idString);
            return exerciseLogRepository.findAllByExerciseTypeId(id);
        }
        return exerciseLogRepository.findAllByExerciseTypeName(exerciseName);
    }

    public List<ExerciseLog> getAllByUserAndExerciseName(User user, String exerciseName) {
        if (exerciseName.startsWith("#")) {
            var idString = exerciseName.substring(1);
            var id = Long.parseLong(idString);
            return exerciseLogRepository.findAllByUserAndExerciseTypeId(user, id);
        }
        return exerciseLogRepository.findAllByUserAndExerciseTypeName(user, exerciseName);
    }

    public LogCreationStatus create(User user, String exerciseName, Double amount) {
        var achievements = new LinkedList<UserAchievement>();
        var exerciseType = exerciseTypeService.getByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));
        var allExistingLogs = getAllByUserAndExerciseName(user, exerciseName);
        var exerciseLog = new ExerciseLog(amount, exerciseType, user);
        var exerciseRecord = exerciseType.getExerciseRecords().stream().filter(r -> r.getUser().equals(user)).findFirst().orElse(null);

        long earnedXp = ServiceConstants.EarnedXpLogCreated;
        var newRecordReached = false;
        var goalReached = false;

        if (exerciseRecord == null) {
            var newRecord = new ExerciseRecord(user, exerciseType, amount);
            exerciseType.getExerciseRecords().add(newRecord);
            exerciseRecord = exerciseRecordRepository.save(newRecord);
            earnedXp += ServiceConstants.EarnedXpNewExerciseStarted;
        } else if (ServiceHelper.isRecordImproved(exerciseRecord, exerciseLog)) {
            exerciseRecord.setAmount(exerciseLog.getAmount());
            exerciseRecord = exerciseRecordRepository.save(exerciseRecord);
            newRecordReached = true;
            earnedXp += ServiceConstants.EarnedXpRecordImproved;
        }

        var goalOpt = goalService.getActiveUserGoal(user, exerciseName);
        if (goalOpt.isPresent()) {
            var goal = goalOpt.get();
            if (ServiceHelper.isGoalReached(goal, exerciseLog)) {
                goal.setStatus(GoalStatus.COMPLETED);
                goal.setCompletedAt(LocalDateTime.now());
                exerciseLog.setReachedGoal(goal);
                earnedXp += ServiceConstants.EarnedXpGoalCompleted;
                goalReached = true;
                achievements.addAll(achievementsRulesHandler.checkGoalCompletedAchievements(user, goal));
            }
        }

        achievements.addAll(achievementsRulesHandler.checkLogCreationAchievements(user, exerciseLog));

        userExperienceService.addExperience(user.getId(), earnedXp);

        earnedXp += achievements.stream().mapToLong(a -> UserAchievementService.getEarnedAchievementXp(a.getAchievement())).sum();

        var newExerciseLog = exerciseLogRepository.save(exerciseLog);
        return new LogCreationStatus(
                newExerciseLog,
                allExistingLogs.stream().skip(allExistingLogs.isEmpty() ? 0 : allExistingLogs.size()-1).findFirst().orElse(null),
                allExistingLogs.size() + 1,
                exerciseRecord,
                goalOpt.orElse(null),
                achievements,
                earnedXp,
                allExistingLogs.isEmpty(),
                newRecordReached,
                goalReached
        );
    }
}
