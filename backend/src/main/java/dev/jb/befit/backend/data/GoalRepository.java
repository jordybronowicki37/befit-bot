package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    Optional<Goal> findByUserAndId(User user, Long id);
    List<Goal> findAllByUser(User user);
    List<Goal> findAllByUserAndStatus(User user, GoalStatus status);
    List<Goal> findAllByUserAndCompletedAtAfterAndCompletedAtNotNull(User user, LocalDateTime from);
    List<Goal> findAllByExerciseTypeName(String exerciseTypeName);
    List<Goal> findAllByUserAndExerciseTypeId(User user, Long exerciseTypeId);
    List<Goal> findAllByUserAndExerciseTypeName(User user, String exerciseTypeName);
}
