package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {
    long countAllByUser(User user);
    @Query("select count(distinct e.exerciseType) from ExerciseLog e where e.user = :user")
    long countDistinctExerciseTypeByUser(User user);
    List<ExerciseLog> findAllByUser(User user);
    Page<ExerciseLog> findAllByUserOrderByCreatedDesc(User user, Pageable pageable);
    List<ExerciseLog> findAllByUserAndCreatedAfter(User user, LocalDateTime from);
    List<ExerciseLog> findAllByExerciseTypeId(Long exerciseTypeId);
    List<ExerciseLog> findAllByExerciseTypeName(String exerciseTypeName);
    List<ExerciseLog> findAllByUserAndExerciseTypeId(User user, Long exerciseTypeId);
    List<ExerciseLog> findAllByUserAndExerciseTypeName(User user, String exerciseTypeName);
}
