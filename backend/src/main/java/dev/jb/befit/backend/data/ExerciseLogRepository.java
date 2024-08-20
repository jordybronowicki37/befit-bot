package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {
    List<ExerciseLog> findAllByUser(User user);
    List<ExerciseLog> findAllByExerciseTypeId(Long id);
    List<ExerciseLog> findAllByExerciseTypeName(String name);
    List<ExerciseLog> findAllByUserAndExerciseTypeId(User user, Long id);
    List<ExerciseLog> findAllByUserAndExerciseTypeName(User user, String name);
}
