package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
}
