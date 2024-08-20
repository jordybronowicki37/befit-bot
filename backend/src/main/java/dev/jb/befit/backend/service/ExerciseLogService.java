package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.data.models.User;
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
    private final ExerciseTypeService exerciseTypeService;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final GoalService goalService;

    public List<ExerciseLog> getAllByUser(User user) {
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

    public ExerciseLog create(User user, String exerciseName, Integer amount) {
        var exerciseType = exerciseTypeService.getByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));
        var exerciseLog = new ExerciseLog(amount, exerciseType, user);
        var exerciseRecord = exerciseType.getExerciseRecords().stream().filter(r -> r.getUser().equals(user)).findFirst();

        if (exerciseRecord.isEmpty()) {
            var newRecord = new ExerciseRecord(user, exerciseType, amount);
            exerciseType.getExerciseRecords().add(newRecord);
            exerciseRecordRepository.save(newRecord);
        }
        else if (ServiceHelper.isRecordImproved(exerciseRecord.get(), exerciseLog)) {
            exerciseRecord.get().setAmount(exerciseLog.getAmount());
        }

        goalService.getActiveUserGoal(user, exerciseName).ifPresent(goal -> {
            if (!ServiceHelper.isGoalReached(goal, exerciseLog)) return;
            goal.setStatus(GoalStatus.COMPLETED);
            exerciseLog.setReachedGoal(goal);
        });

        return exerciseLogRepository.save(exerciseLog);
    }
}
