package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseTypeRepository;
import dev.jb.befit.backend.data.models.ExerciseType;
import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.MeasurementTypes;
import dev.jb.befit.backend.service.exceptions.InvalidExerciseNameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseTypeService {
    private final ExerciseTypeRepository exerciseTypeRepository;

    public List<ExerciseType> getAll() {
        return exerciseTypeRepository.findAll();
    }

    public Page<ExerciseType> getPage(Pageable pageable) {
        return exerciseTypeRepository.findAll(pageable);
    }

    public Optional<ExerciseType> getByName(String name) {
        if (name.startsWith("#")) {
            var idString = name.substring(1);
            var id = Long.parseLong(idString);
            return exerciseTypeRepository.findById(id);
        }
        return exerciseTypeRepository.findByName(name);
    }

    public ExerciseType create(String name, MeasurementTypes measurementType, GoalDirection goalDirection) {
        if (name.startsWith("#")) throw new InvalidExerciseNameException("An exercise name can not start with '#'");
        var exerciseType = new ExerciseType(name, measurementType, goalDirection);
        return exerciseTypeRepository.save(exerciseType);
    }
}
