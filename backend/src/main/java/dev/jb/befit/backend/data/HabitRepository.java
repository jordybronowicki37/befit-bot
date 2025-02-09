package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.data.models.User;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    Optional<Habit> findHabitByUserAndId(@NonNull User user, @NonNull Long id);
    List<Habit> findAllByUserAndDeletedFalse(@NonNull User user);
    List<Habit> findAllByUserAndHabitTimeRangeAndDeletedFalse(@NonNull User user, @NonNull HabitTimeRange habitTimeRange);
    Page<Habit> findAllByUserAndNameIgnoreCaseContainingAndDeletedFalseOrderByCreatedDesc(@NonNull User user, @NonNull String name, @NonNull Pageable pageable);
}
