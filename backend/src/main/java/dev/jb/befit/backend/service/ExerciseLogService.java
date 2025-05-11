package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.UserAchievementsRepository;
import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.service.dto.LogCreationStatus;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import dev.jb.befit.backend.service.exceptions.InvalidUserException;
import dev.jb.befit.backend.service.exceptions.LogNotFoundException;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseLogService {
    private final AchievementsRulesHandler achievementsRulesHandler;
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseSessionService exerciseSessionService;
    private final ExerciseTypeService exerciseTypeService;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseRecordService exerciseRecordService;
    private final GoalService goalService;
    private final UserAchievementsRepository userAchievementsRepository;
    private final UserExperienceService userExperienceService;

    public List<ExerciseLog> getAllByUser(User user) {
        return exerciseLogRepository.findAllByUser(user);
    }

    public Optional<ExerciseLog> getByUserAndId(User user, Long id) {
        var log = exerciseLogRepository.findById(id);
        if (log.isPresent() && !log.get().getUser().equals(user)) {
            throw new InvalidUserException();
        }
        return log;
    }

    public long countAllByUser(User user) {
        return exerciseLogRepository.countAllByUser(user);
    }

    public long countAmountOfExercisesByUser(User user) {
        return exerciseLogRepository.countDistinctExerciseTypeByUser(user);
    }

    public Optional<ExerciseLog> getLastByUser(User user) {
        return exerciseLogRepository.findTopByUserOrderByIdDesc(user);
    }

    public Page<ExerciseLog> getAllRecentByUser(User user, Pageable pageable) {
        return exerciseLogRepository.findAllByUserOrderByCreatedDesc(user, pageable);
    }

    public Page<ExerciseLog> getAllRecentByUser(User user, Long exerciseTypeId, Pageable pageable) {
        return exerciseLogRepository.findAllByUserAndExerciseTypeIdOrderByCreatedDesc(user, exerciseTypeId, pageable);
    }

    public List<ExerciseLog> getAllExpiredUndoSchedules() {
        return exerciseLogRepository.findAllByCreatedBeforeAndDiscordChannelIdNotNullAndDiscordMessageIdNotNull(LocalDateTime.now().minusSeconds(ServiceConstants.LogUndoExpireTimeout.getEpochSecond()));
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
            var id = ServiceHelper.getIdFromExerciseString(exerciseName);
            return exerciseLogRepository.findAllByUserAndExerciseTypeIdOrderByCreatedAsc(user, id);
        }
        return exerciseLogRepository.findAllByUserAndExerciseTypeNameOrderByCreatedAsc(user, exerciseName);
    }

    public ExerciseLog scheduleUndoExpiry(User user, Long id, Snowflake channelId, Snowflake messageId) {
        var exerciseLog = getByUserAndId(user, id).orElseThrow(() -> new LogNotFoundException(id));
        exerciseLog.setDiscordChannelId(channelId);
        exerciseLog.setDiscordMessageId(messageId);
        return exerciseLogRepository.save(exerciseLog);
    }

    public ExerciseLog removeUndoExpiry(User user, Long id) {
        var exerciseLog = getByUserAndId(user, id).orElseThrow(() -> new LogNotFoundException(id));
        exerciseLog.setDiscordChannelId(null);
        exerciseLog.setDiscordMessageId(null);
        return exerciseLogRepository.save(exerciseLog);
    }

    public LogCreationStatus create(User user, String exerciseName, Double amount) {
        var achievements = new LinkedList<UserAchievement>();
        var exerciseType = exerciseTypeService.getByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));
        var allExistingLogs = getAllByUserAndExerciseName(user, exerciseName);
        var lastExerciseLog = allExistingLogs.stream().skip(allExistingLogs.isEmpty() ? 0 : allExistingLogs.size()-1).findFirst().orElse(null);
        var exerciseLog = new ExerciseLog(amount, exerciseType, user);
        exerciseLog.setFirstLogOfExercise(allExistingLogs.isEmpty());
        var exerciseRecord = exerciseRecordService.getByExercise(user, exerciseType).orElse(null);
        var earnedXp = ServiceConstants.EarnedXpLogCreated;
        var oldUserXp = user.getXp();

        // Add session if one is active
        var session = exerciseSessionService.getLastActiveByUser(user).orElse(null);
        if (session != null) {
            exerciseLog.setSession(session);
            session.getExerciseLogs().add(exerciseLog);
            earnedXp += ServiceConstants.EarnedXpLogAddedToSession;
            exerciseSessionService.extendAutomaticFinalization(user, session.getId());
        }

        // Add a record if none is found, else update the record if it is improved
        if (exerciseRecord == null) {
            earnedXp += ServiceConstants.EarnedXpNewExerciseStarted;
        } else if (ExerciseRecordService.isRecordImproved(exerciseRecord, exerciseLog)) {
            exerciseLog.setPrImproved(true);
            earnedXp += ServiceConstants.EarnedXpRecordImproved;
        }
        exerciseRecord = exerciseRecordService.createOrUpdate(user, exerciseLog);

        // Finish goal if it exists and is reached
        var goal = goalService.getActiveUserGoal(user, exerciseName).orElse(null);
        if (goal != null) {
            if (GoalService.isGoalReached(goal, exerciseLog)) {
                goal.setStatus(GoalStatus.COMPLETED);
                goal.setCompletedAt(LocalDateTime.now());
                exerciseLog.setReachedGoal(goal);
                earnedXp += ServiceConstants.EarnedXpGoalCompleted;
                achievements.addAll(achievementsRulesHandler.checkGoalCompletedAchievements(user, goal));
            }
        }

        // Check and then add achievements
        achievements.addAll(achievementsRulesHandler.checkLogCreationAchievements(user, exerciseLog));
        exerciseLog.getAchievements().addAll(achievements);

        // Finalize earned xp
        earnedXp += achievements.stream().mapToLong(a -> UserAchievementService.getEarnedAchievementXp(a.getAchievement())).sum();
        exerciseLog.setEarnedXp(earnedXp);
        userExperienceService.addExperience(user.getId(), earnedXp);
        var oldXpLevelData = UserExperienceService.getLevelData(oldUserXp);
        var newXpLevelData = UserExperienceService.getLevelData(oldUserXp+earnedXp);
        if (oldXpLevelData.level() < newXpLevelData.level()) exerciseLog.setLevelCompleted(true);

        // Save the log and then save the achievements so that they get linked correctly
        var newExerciseLog = exerciseLogRepository.save(exerciseLog);
        achievements.forEach(a -> a.setLog(newExerciseLog));
        userAchievementsRepository.saveAll(achievements);

        return new LogCreationStatus(
                newExerciseLog,
                lastExerciseLog,
                allExistingLogs.size() + 1,
                exerciseRecord,
                goal
        );
    }

    public ExerciseLog undoLogCreation(User user, Long logId) {
        var log = getByUserAndId(user, logId).orElseThrow(() -> new LogNotFoundException(logId));

        if (log.isPrImproved()) {
            var exerciseType = log.getExerciseType();
            var logsStream = getAllByUserAndExerciseName(user, exerciseType.getName()).stream().filter(l -> !l.equals(log)).map(ExerciseLog::getAmount);
            var oldPr = exerciseType.getGoalDirection().equals(GoalDirection.INCREASING) ? logsStream.max(Double::compareTo) : logsStream.min(Double::compareTo);
            var record = exerciseRecordService.getByExercise(user, exerciseType);

            if (oldPr.isPresent()) {
                if (record.isPresent()) {
                    record.get().setAmount(oldPr.get());
                    exerciseRecordRepository.save(record.get());
                } else {
                    exerciseRecordRepository.save(new ExerciseRecord(user, exerciseType, oldPr.get()));
                }
            } else {
                record.ifPresent(exerciseRecordRepository::delete);
            }
        }

        if (log.getReachedGoal() != null) {
            var goal = log.getReachedGoal();
            goal.setStatus(GoalStatus.ACTIVE);
            goal.setCompletedAt(null);
        }

        userAchievementsRepository.deleteAll(log.getAchievements());
        user.setXp(user.getXp() - log.getEarnedXp());

        exerciseLogRepository.delete(log);

        return log;
    }
}
