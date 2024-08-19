package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseTypeRepository;
import dev.jb.befit.backend.data.GoalRepository;
import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    public List<Goal> getAllByUser(User user) {
        return goalRepository.findAllByUser(user);
    }

    public List<Goal> getAllByExerciseName(String exerciseName) {
        return goalRepository.findAllByExerciseTypeName(exerciseName);
    }

    public List<Goal> getAllByUserAndExerciseName(User user, String exerciseName) {
        return goalRepository.findAllByUserAndExerciseTypeName(user, exerciseName);
    }

    public List<Goal> getAllActiveUserGoals(User user) {
        return getAllByUser(user).stream().filter(g -> g.getStatus().equals(GoalStatus.ACTIVE)).toList();
    }

    public Optional<Goal> getActiveUserGoal(User user, String exerciseName) {
        var activeUserGoals = getAllByUserAndExerciseName(user, exerciseName).stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).toList();
        if (activeUserGoals.isEmpty()) return Optional.empty();
        if (activeUserGoals.size() == 1) return Optional.of(activeUserGoals.get(0));
        log.error("More than 1 goal???");
        // TODO cancel other goals
        return Optional.of(activeUserGoals.get(activeUserGoals.size() - 1));
    }

    public Goal create(User user, String exerciseName, Integer amount) {
        var exerciseType = exerciseTypeRepository.findByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));

        // If a goal already exists, change the status to overwritten
        getActiveUserGoal(user, exerciseName).ifPresent(lastGoal -> {
            lastGoal.setStatus(GoalStatus.OVERWRITTEN);
            goalRepository.save(lastGoal);
        });

        var goal = new Goal(amount, exerciseType, user);
        goal.setStatus(GoalStatus.ACTIVE);
        return goalRepository.save(goal);
    }
}
