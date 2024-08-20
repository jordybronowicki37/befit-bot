package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findAllByUser(User user);
    List<Goal> findAllByExerciseTypeName(String name);
    List<Goal> findAllByUserAndExerciseTypeId(User user, Long id);
    List<Goal> findAllByUserAndExerciseTypeName(User user, String name);
}
