package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.service.exceptions.ExerciseMismatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseRecordService {
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseTypeService exerciseTypeService;

    public List<ExerciseRecord> getAllByUser(User user) {
        return exerciseRecordRepository.findAllByUser(user);
    }

    public List<ExerciseRecord> getAll() {
        return exerciseRecordRepository.findAll();
    }

    public List<ExerciseRecord> getFiltered(User user, String filter) {
        return exerciseRecordRepository.findByUserAndExerciseTypeNameIgnoreCaseContaining(user, filter);
    }

    public Optional<ExerciseRecord> getByExercise(User user, String exerciseName) {
        var exercise = exerciseTypeService.findByName(exerciseName);
        return getByExercise(user, exercise);
    }

    public Optional<ExerciseRecord> getByExercise(User user, ExerciseType exerciseType) {
        return exerciseRecordRepository.findByUserAndExerciseType(user, exerciseType);
    }

    public ExerciseRecord createOrUpdate(User user, ExerciseLog exerciseLog) {
        var exerciseType = exerciseLog.getExerciseType();
        var record = getByExercise(user, exerciseType).orElse(null);
        if (record == null) {
            record = exerciseRecordRepository.save(new ExerciseRecord(user, exerciseType, exerciseLog, exerciseLog.getAmount()));
        } else if (isRecordImproved(record, exerciseLog)) {
            record.setAmount(exerciseLog.getAmount());
            record.setExerciseLog(exerciseLog);
            record = exerciseRecordRepository.save(record);
        }
        return record;
    }

    public static boolean isPRImproved(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return false;
        if (allExerciseLogs.size() == 1) return true;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        if (!allExerciseLogs.stream().allMatch(l -> l.getExerciseType() == exerciseType)) throw new ExerciseMismatchException();

        var exerciseAmounts = new ArrayList<>(allExerciseLogs.stream().map(ExerciseLog::getAmount).toList());
        var lastValue = exerciseAmounts.remove(exerciseAmounts.size() - 1);

        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            var maxValue = exerciseAmounts.stream().max(Double::compareTo);
            return maxValue.filter(integer -> integer < lastValue).isPresent();
        } else {
            var minValue = exerciseAmounts.stream().min(Double::compareTo);
            return minValue.filter(integer -> integer > lastValue).isPresent();
        }
    }

    public static Double getCurrentPr(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return null;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        if (!allExerciseLogs.stream().allMatch(l -> l.getExerciseType() == exerciseType)) throw new ExerciseMismatchException();
        var exerciseAmounts = allExerciseLogs.stream().map(ExerciseLog::getAmount).toList();
        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseAmounts.stream().max(Double::compareTo).orElse(null);
        } else {
            return exerciseAmounts.stream().min(Double::compareTo).orElse(null);
        }
    }

    public static boolean isRecordImproved(ExerciseRecord exerciseRecord, ExerciseLog exerciseLog) {
        if (!exerciseRecord.getExerciseType().equals(exerciseLog.getExerciseType())) throw new ExerciseMismatchException();
        if (ServiceHelper.compareDoublesWithTolerance(exerciseLog.getAmount(), exerciseRecord.getAmount())) return false;

        if (exerciseRecord.getExerciseType().getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseLog.getAmount() > exerciseRecord.getAmount();
        }
        else {
            return exerciseLog.getAmount() < exerciseRecord.getAmount();
        }
    }
}
