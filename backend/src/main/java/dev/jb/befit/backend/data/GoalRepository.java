package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseType;
import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.data.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findAllByUser(@NonNull User user);
    List<Goal> findAllByUserAndStatus(@NonNull User user, @NonNull GoalStatus status);
    List<Goal> findAllByUserAndCompletedAtAfterAndCompletedAtNotNull(@NonNull User user, @NonNull LocalDateTime from);
    List<Goal> findAllByExerciseType(@NonNull ExerciseType exerciseType);
    List<Goal> findAllByUserAndExerciseType(@NonNull User user, @NonNull ExerciseType exerciseType);
}
