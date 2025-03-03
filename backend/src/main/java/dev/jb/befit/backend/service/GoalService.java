package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseTypeRepository;
import dev.jb.befit.backend.data.GoalRepository;
import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import dev.jb.befit.backend.service.exceptions.GoalNotFoundException;
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
    private final UserExperienceService userExperienceService;

    public Optional<Goal> getByUserAndId(User user, Long id) {
        return goalRepository.findByUserAndId(user, id);
    }

    public List<Goal> getAllUserGoals(User user) {
        return goalRepository.findAllByUser(user);
    }

    public List<Goal> getAllUserGoals(User user, GoalStatus status) {
        return goalRepository.findAllByUserAndStatus(user, status);
    }

    public List<Goal> getAllByExerciseName(String exerciseName) {
        return goalRepository.findAllByExerciseTypeName(exerciseName);
    }

    public List<Goal> getAllByUserAndExerciseName(User user, String exerciseName) {
        if (exerciseName.startsWith("#")) {
            var idString = exerciseName.substring(1);
            var id = Long.parseLong(idString);
            return goalRepository.findAllByUserAndExerciseTypeId(user, id);
        }
        return goalRepository.findAllByUserAndExerciseTypeName(user, exerciseName);
    }

    public Optional<Goal> getActiveUserGoal(User user, String exerciseName) {
        var activeUserGoals = getAllByUserAndExerciseName(user, exerciseName).stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).toList();
        if (activeUserGoals.isEmpty()) return Optional.empty();
        if (activeUserGoals.size() == 1) return Optional.of(activeUserGoals.get(0));

        log.error("User {} has more than one goal for exercise {}", user, exerciseName);

        for (int i = 0; i < activeUserGoals.size(); i++) {
            var goal = activeUserGoals.get(i);
            if (i == activeUserGoals.size() - 1) return Optional.of(goal);
            goal.setStatus(GoalStatus.OVERWRITTEN);
            goalRepository.save(goal);
        }

        return Optional.empty();
    }

    public Goal create(User user, String exerciseName, Double amount) {
        var exerciseType = exerciseTypeRepository.findByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));

        // If a goal already exists, change the status to overwritten
        getActiveUserGoal(user, exerciseName).ifPresent(lastGoal -> {
            lastGoal.setStatus(GoalStatus.OVERWRITTEN);
            goalRepository.save(lastGoal);
        });

        userExperienceService.addExperience(user, ServiceConstants.EarnedXpGoalCreated);

        var goal = new Goal(amount, exerciseType, user);
        goal.setStatus(GoalStatus.ACTIVE);
        return goalRepository.save(goal);
    }

    public Goal cancel(User user, Long goalId) {
        var goalOpt = getByUserAndId(user, goalId);
        if (goalOpt.isEmpty()) throw new GoalNotFoundException(goalId);
        var goal = goalOpt.get();
        goal.setStatus(GoalStatus.CANCELLED);
        goalRepository.save(goal);
        return goal;
    }
}
