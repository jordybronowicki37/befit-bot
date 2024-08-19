package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, ExerciseRecord.ExerciseRecordId> {
}
