package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.ExerciseSessionStatus;
import dev.jb.befit.backend.data.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, Long> {
    Long countAllByUser(User user);
    Page<ExerciseSession> findAllByUserOrderByCreatedDesc(User user, Pageable pageable);
    Page<ExerciseSession> findAllByUserAndStatusOrderByCreatedDesc(User user, ExerciseSessionStatus status, Pageable pageable);
    Page<ExerciseSession> findAllByUserAndNameIgnoreCaseContainingOrderByCreatedDesc(User user, String name, Pageable pageable);
    Optional<ExerciseSession> findByUserAndId(User user, Long id);
    Optional<ExerciseSession> findTopByUserOrderByIdDesc(User user);
}
