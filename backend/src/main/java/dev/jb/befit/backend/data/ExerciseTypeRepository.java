package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.ExerciseType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseTypeRepository extends JpaRepository<ExerciseType, Long> {
    Optional<ExerciseType> findByName(@NonNull String name);
    List<ExerciseType> findByNameIgnoreCaseContaining(@NonNull String name);
}
