package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    Optional<HabitLog> findByHabitIdAndLogDate(Long habitId, LocalDate date);
}
