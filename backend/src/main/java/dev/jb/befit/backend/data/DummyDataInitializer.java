package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.*;
import discord4j.common.util.Snowflake;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
@Profile("dummy-data")
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final GoalRepository goalRepository;

    @Value("${discord.dummy-user-id}")
    private String userId;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Started dummy-data initialization");
        var discordUser = new DiscordUser(Snowflake.of(userId));
        var webUser1 = new WebUser("test-user", "test@example.com", "12345678");
        var webUser2 = new WebUser("other-test-user", "other-test@example.com", "12345678");
        userRepository.saveAll(List.of(discordUser, webUser1, webUser2));

        var exercises = IntStream.range(1, 20).mapToObj(i -> new ExerciseType("exercise-"+i, MeasurementTypes.KG, GoalDirection.INCREASING)).toList();
        exerciseTypeRepository.saveAll(exercises);
        var myLogs = exercises.stream().map(e -> new ExerciseLog(10, e, discordUser)).toList();
        exerciseLogRepository.saveAll(myLogs);
        var myRecords = myLogs.stream().map(e -> new ExerciseRecord(discordUser, e.getExerciseType(), e.getAmount())).toList();
        exerciseRecordRepository.saveAll(myRecords);

        var benchpress = new ExerciseType("Benchpress", MeasurementTypes.KG, GoalDirection.INCREASING);
        var running = new ExerciseType("running", MeasurementTypes.KM, GoalDirection.INCREASING);
        exerciseTypeRepository.saveAll(List.of(benchpress, running));

        var log1 = new ExerciseLog(20, benchpress, discordUser);
        log1.setCreated(LocalDateTime.now().minusDays(8));
        var log2 = new ExerciseLog(23, benchpress, discordUser);
        log2.setCreated(LocalDateTime.now().minusDays(5));
        var log3 = new ExerciseLog(31, benchpress, discordUser);
        log3.setCreated(LocalDateTime.now().minusDays(3));
        var log4 = new ExerciseLog(45, benchpress, discordUser);
        log4.setCreated(LocalDateTime.now().minusDays(1));
        var log5 = new ExerciseLog(50, benchpress, webUser1);
        log5.setCreated(LocalDateTime.now().minusMinutes(120));
        var log6 = new ExerciseLog(55, benchpress, webUser2);
        log6.setCreated(LocalDateTime.now().minusMinutes(60));
        exerciseLogRepository.saveAll(List.of(log1, log2, log3, log4, log5, log6));

        exerciseRecordRepository.save(new ExerciseRecord(discordUser, benchpress, 45));
        exerciseRecordRepository.save(new ExerciseRecord(webUser1, benchpress, 50));
        exerciseRecordRepository.save(new ExerciseRecord(webUser2, benchpress, 55));

        goalRepository.save(new Goal(50, benchpress, discordUser));

        log.info("Finished dummy-data initialization");
    }
}
