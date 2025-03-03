package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.ExerciseType;
import dev.jb.befit.backend.data.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, ExerciseRecord.ExerciseRecordId> {
    List<ExerciseRecord> findAllByUser(@NonNull User user);
    Optional<ExerciseRecord> findByUserAndExerciseType(@NonNull User user, @NonNull ExerciseType exerciseType);
    List<ExerciseRecord> findByUserAndExerciseTypeNameIgnoreCaseContaining(@NonNull User user, @NonNull String name);
}
