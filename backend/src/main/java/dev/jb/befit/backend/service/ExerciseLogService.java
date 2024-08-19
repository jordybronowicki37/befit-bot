package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.ExerciseTypeRepository;
import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseLogService {
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final GoalService goalService;

    public List<ExerciseLog> getAllByUser(User user) {
        return exerciseLogRepository.findAllByUser(user);
    }

    public List<ExerciseLog> getAllByExerciseName(String exerciseName) {
        return exerciseLogRepository.findAllByExerciseTypeName(exerciseName);
    }

    public List<ExerciseLog> getAllByUserAndExerciseName(User user, String exerciseName) {
        return exerciseLogRepository.findAllByUserAndExerciseTypeName(user, exerciseName);
    }

    public ExerciseLog create(User user, String exerciseName, Integer amount) {
        var exerciseType = exerciseTypeRepository.findByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));
        var exerciseLog = new ExerciseLog(amount, exerciseType, user);
        var exerciseRecord = exerciseType.getExerciseRecords().stream().filter(r -> r.getUser().equals(user)).findFirst();

        if (exerciseRecord.isEmpty()) {
            var newRecord = new ExerciseRecord(user, exerciseType, amount);
            exerciseType.getExerciseRecords().add(newRecord);
            exerciseRecordRepository.save(newRecord);
        }
        else {
            if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
                if (exerciseLog.getAmount() > exerciseRecord.get().getAmount()) {
                    exerciseRecord.get().setAmount(exerciseLog.getAmount());
                }
            }
            else {
                if (exerciseLog.getAmount() < exerciseRecord.get().getAmount()) {
                    exerciseRecord.get().setAmount(exerciseLog.getAmount());
                }
            }
        }

        goalService.getActiveUserGoal(user, exerciseName).ifPresent(goal -> {
            boolean isReached;
            if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
                isReached = exerciseLog.getAmount() >= goal.getAmount();
            }
            else {
                isReached = exerciseLog.getAmount() <= goal.getAmount();
            }
            if (!isReached) return;
            goal.setStatus(GoalStatus.COMPLETED);
            exerciseLog.setReachedGoal(goal);
        });

        return exerciseLogRepository.save(exerciseLog);
    }
}
