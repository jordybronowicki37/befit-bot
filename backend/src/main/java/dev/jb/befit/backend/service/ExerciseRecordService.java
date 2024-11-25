package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseRecordService {
    private final ExerciseRecordRepository exerciseRecordRepository;

    public List<ExerciseRecord> getAllByUser(User user) {
        return exerciseRecordRepository.findAllByUser(user);
    }

    public List<ExerciseRecord> getAll() {
        return exerciseRecordRepository.findAll();
    }
}
