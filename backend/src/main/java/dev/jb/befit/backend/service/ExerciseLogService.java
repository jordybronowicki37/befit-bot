package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseTypeRepository;
import dev.jb.befit.backend.data.models.ExerciseLog;
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
    private final ExerciseTypeRepository exerciseTypeRepository;

    public List<ExerciseLog> getAllByUser(User user) {
        return exerciseLogRepository.findAllByUser(user);
    }

    public List<ExerciseLog> getAllByExerciseName(String name) {
        return exerciseLogRepository.findAllByExerciseTypeName(name);
    }

    public List<ExerciseLog> getAllByUserAndExerciseName(User user, String name) {
        return exerciseLogRepository.findAllByUserAndExerciseTypeName(user, name);
    }

    public ExerciseLog create(User user, String exerciseName, Integer amount) {
        var exerciseType = exerciseTypeRepository.findByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));
        var exerciseLog = new ExerciseLog(amount, exerciseType, user);
        return exerciseLogRepository.save(exerciseLog);
    }
}
