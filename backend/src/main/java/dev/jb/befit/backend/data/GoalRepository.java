package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.data.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    Optional<Goal> findByUserAndId(@NonNull User user, @NonNull Long id);
    List<Goal> findAllByUser(@NonNull User user);
    List<Goal> findAllByUserAndStatus(@NonNull User user, @NonNull GoalStatus status);
    List<Goal> findAllByUserAndCompletedAtAfterAndCompletedAtNotNull(@NonNull User user, @NonNull LocalDateTime from);
    List<Goal> findAllByExerciseTypeName(@NonNull String exerciseTypeName);
    List<Goal> findAllByUserAndExerciseTypeId(@NonNull User user, @NonNull Long exerciseTypeId);
    List<Goal> findAllByUserAndExerciseTypeName(@NonNull User user, @NonNull String exerciseTypeName);
}
