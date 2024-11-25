package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, ExerciseRecord.ExerciseRecordId> {
    List<ExerciseRecord> findAllByUser(User user);
}
