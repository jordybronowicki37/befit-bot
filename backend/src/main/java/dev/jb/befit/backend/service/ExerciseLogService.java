package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.service.dto.LogCreationStatus;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserService userService;
    private final UserExperienceService userExperienceService;

    public List<ExerciseLog> getAllByUser(User user) {
        return exerciseLogRepository.findAllByUser(user);
    }

    public List<ExerciseLog> getAllByUser(User user, LocalDateTime from) {
        return exerciseLogRepository.findAllByUser(user);
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

        long earnedXp = 10;
        var newRecordReached = false;
        var goalReached = false;

        if (exerciseRecord == null) {
            var newRecord = new ExerciseRecord(user, exerciseType, amount);
            exerciseType.getExerciseRecords().add(newRecord);
            exerciseRecord = exerciseRecordRepository.save(newRecord);
            earnedXp += 20;
        } else if (ServiceHelper.isRecordImproved(exerciseRecord, exerciseLog)) {
            exerciseRecord.setAmount(exerciseLog.getAmount());
            exerciseRecord = exerciseRecordRepository.save(exerciseRecord);
            newRecordReached = true;
            earnedXp += 50;
        }

        var goalOpt = goalService.getActiveUserGoal(user, exerciseName);
        if (goalOpt.isPresent()) {
            var goal = goalOpt.get();
            if (ServiceHelper.isGoalReached(goal, exerciseLog)) {
                goal.setStatus(GoalStatus.COMPLETED);
                goal.setCompletedAt(LocalDateTime.now());
                exerciseLog.setReachedGoal(goal);
                earnedXp += 50;
                goalReached = true;
                achievements.addAll(achievementsRulesHandler.checkGoalCompletedAchievements(user, goal));
            }
        }

        achievements.addAll(achievementsRulesHandler.checkLogCreationAchievements(user, exerciseLog));

        userExperienceService.addExperience(user.getId(), earnedXp);
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
