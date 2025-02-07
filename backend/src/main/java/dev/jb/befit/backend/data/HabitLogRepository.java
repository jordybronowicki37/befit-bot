package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.HabitLog;
import dev.jb.befit.backend.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    Optional<HabitLog> findByUserAndHabitIdAndLogDate(User user, Long habitId, LocalDate date);
}
