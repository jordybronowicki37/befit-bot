package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.User;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {
    long countAllByUser(@NonNull User user);
    @Query("select count(distinct e.exerciseType) from ExerciseLog e where e.user = :user")
    long countDistinctExerciseTypeByUser(@NonNull @Param("user") User user);
    List<ExerciseLog> findAllByUser(@NonNull User user);
    Page<ExerciseLog> findAllByUserOrderByCreatedDesc(@NonNull User user, @NonNull Pageable pageable);
    List<ExerciseLog> findAllByUserAndExerciseTypeIdOrderByCreatedAsc(@NonNull User user, @NonNull Long exerciseTypeId);
    Page<ExerciseLog> findAllByUserAndExerciseTypeIdOrderByCreatedDesc(@NonNull User user, @NonNull Long exerciseTypeId, @NonNull Pageable pageable);
    List<ExerciseLog> findAllByUserAndExerciseTypeNameOrderByCreatedAsc(@NonNull User user, @NonNull String exerciseTypeName);
    List<ExerciseLog> findAllByCreatedBeforeAndDiscordChannelIdNotNullAndDiscordMessageIdNotNull(@NonNull LocalDateTime dateBefore);
    List<ExerciseLog> findAllByUserAndCreatedAfter(@NonNull User user, @NonNull LocalDateTime from);
    List<ExerciseLog> findAllByExerciseTypeId(@NonNull Long exerciseTypeId);
    List<ExerciseLog> findAllByExerciseTypeName(@NonNull String exerciseTypeName);
    Optional<ExerciseLog> findTopByUserOrderByIdDesc(@NonNull User user);
    Optional<ExerciseLog> findTopByUserAndExerciseTypeIdOrderByIdDesc(@NonNull User user, @NonNull Long exerciseTypeId);
}
