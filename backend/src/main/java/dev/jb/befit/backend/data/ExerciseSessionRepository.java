package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.ExerciseSessionStatus;
import dev.jb.befit.backend.data.models.User;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, Long> {
    Long countAllByUser(@NonNull User user);
    Page<ExerciseSession> findAllByUserOrderByCreatedDesc(@NonNull User user, @NonNull Pageable pageable);
    Page<ExerciseSession> findAllByUserAndStatusOrderByCreatedDesc(@NonNull User user, @NonNull ExerciseSessionStatus status, @NonNull Pageable pageable);
    Page<ExerciseSession> findAllByUserAndNameIgnoreCaseContainingOrderByCreatedDesc(@NonNull User user, @NonNull String name, @NonNull Pageable pageable);
    List<ExerciseSession> findAllByStatusAndEndedBeforeOrderByCreatedAsc(@NonNull ExerciseSessionStatus status, @NonNull LocalDateTime date);
    List<ExerciseSession> findAllByDiscordMessageIdNotNullAndEndedBeforeOrderByCreatedAsc(@NonNull LocalDateTime date);
    Optional<ExerciseSession> findByUserAndId(@NonNull User user, @NonNull Long id);
    Optional<ExerciseSession> findTopByUserOrderByIdDesc(@NonNull User user);
    Optional<ExerciseSession> findTopByUserAndStatusOrderByIdDesc(@NonNull User user, @NonNull ExerciseSessionStatus status);
}
