package dev.jb.befit.backend.discord.registration;

import dev.jb.befit.backend.data.ExerciseLogRepository;
import dev.jb.befit.backend.data.ExerciseRecordRepository;
import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.GoalDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("sync-records")
public class SyncRecordsRunner implements CommandLineRunner {
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseLogRepository exerciseLogRepository;

    @Override
    public void run(String... args) {
        var records = exerciseRecordRepository.findAll();
        records.forEach(record -> {
            var exercise = record.getExerciseType();
            var logsStream = exerciseLogRepository.findAllByUserAndExerciseTypeIdOrderByCreatedAsc(record.getUser(), exercise.getId()).stream();
            var comparator = Comparator.comparing(ExerciseLog::getAmount);
            var topLog = exercise.getGoalDirection().equals(GoalDirection.INCREASING) ? logsStream.max(comparator) : logsStream.min(comparator);
            topLog.ifPresentOrElse(prLog -> {
                record.setExerciseLog(prLog);
                record.setAmount(prLog.getAmount());
                exerciseRecordRepository.save(record);
            }, () -> {
                exerciseRecordRepository.delete(record);
            });
        });
    }
}
