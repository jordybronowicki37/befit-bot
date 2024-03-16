package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseTypeRepository;
import dev.jb.befit.backend.data.models.ExerciseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Optional<ExerciseType> getByName(String name) {
        return exerciseTypeRepository.findByName(name);
    }

    public ExerciseType create(String name) {
        var exerciseType = new ExerciseType(name);
        return exerciseTypeRepository.save(exerciseType);
    }
}
