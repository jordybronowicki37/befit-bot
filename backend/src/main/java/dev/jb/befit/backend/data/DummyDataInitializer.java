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
        discordUser.setXp(810L);
        var webUser1 = new WebUser("test-user", "test@example.com", "12345678");
        var webUser2 = new WebUser("other-test-user", "other-test@example.com", "12345678");
        userRepository.saveAll(List.of(discordUser, webUser1, webUser2));

//        var exercises = IntStream.range(1, 20).mapToObj(i -> new ExerciseType("exercise-"+i, MeasurementTypes.KG, GoalDirection.INCREASING)).toList();
//        exerciseTypeRepository.saveAll(exercises);
//        var myLogs = exercises.stream().map(e -> new ExerciseLog(10, e, discordUser)).toList();
//        exerciseLogRepository.saveAll(myLogs);
//        var myRecords = myLogs.stream().map(e -> new ExerciseRecord(discordUser, e.getExerciseType(), e.getAmount())).toList();
//        exerciseRecordRepository.saveAll(myRecords);

        var benchpress = new ExerciseType("Bench press", MeasurementType.KG, GoalDirection.INCREASING);
        var pullUp = new ExerciseType("Pull ups", MeasurementType.AMOUNT, GoalDirection.INCREASING);
        var running = new ExerciseType("Running 5km", MeasurementType.MINUTES, GoalDirection.DECREASING);
        var pecFly = new ExerciseType("Pec fly", MeasurementType.KG, GoalDirection.INCREASING);
        var shoulderPress = new ExerciseType("Shoulder press", MeasurementType.MINUTES, GoalDirection.INCREASING);
        var cycling = new ExerciseType("Cycling", MeasurementType.KM, GoalDirection.INCREASING);
        exerciseTypeRepository.saveAll(List.of(benchpress, pullUp, running, pecFly, shoulderPress, cycling));

        var benchLog1 = new ExerciseLog(30d, benchpress, discordUser);
        benchLog1.setCreated(LocalDateTime.now().minusDays(30));
        var benchLog2 = new ExerciseLog(35d, benchpress, discordUser);
        benchLog2.setCreated(LocalDateTime.now().minusDays(27));
        var benchLog3 = new ExerciseLog(40d, benchpress, discordUser);
        benchLog3.setCreated(LocalDateTime.now().minusDays(20));
        var benchLog4 = new ExerciseLog(45d, benchpress, discordUser);
        benchLog4.setCreated(LocalDateTime.now().minusDays(15));
        var benchLog5 = new ExerciseLog(50d, benchpress, discordUser);
        benchLog5.setCreated(LocalDateTime.now().minusDays(8));
        var benchLog6 = new ExerciseLog(55d, benchpress, discordUser);
        var benchLog7 = new ExerciseLog(35d, benchpress, webUser1);
        benchLog7.setCreated(LocalDateTime.now().minusDays(18));
        var benchLog8 = new ExerciseLog(40d, benchpress, webUser1);
        benchLog8.setCreated(LocalDateTime.now().minusDays(7));
        var benchLog9 = new ExerciseLog(45d, benchpress, webUser1);
        benchLog9.setCreated(LocalDateTime.now().minusDays(4));
        var benchLog10 = new ExerciseLog(55d, benchpress, webUser2);
        benchLog10.setCreated(LocalDateTime.now().minusDays(13));
        var benchLog11 = new ExerciseLog(50d, benchpress, webUser2);
        benchLog11.setCreated(LocalDateTime.now().minusDays(8));
        var benchLog12 = new ExerciseLog(60d, benchpress, webUser2);
        benchLog12.setCreated(LocalDateTime.now().minusDays(5));
        exerciseRecordRepository.save(new ExerciseRecord(discordUser, benchpress, 55d));
        exerciseRecordRepository.save(new ExerciseRecord(webUser1, benchpress, 45d));
        exerciseRecordRepository.save(new ExerciseRecord(webUser2, benchpress, 60d));
        exerciseLogRepository.saveAll(List.of(benchLog1, benchLog2, benchLog3, benchLog4, benchLog5, benchLog6, benchLog7, benchLog8, benchLog9, benchLog10, benchLog11, benchLog12));

        var pullLog1 = new ExerciseLog(5d, pullUp, discordUser);
        pullLog1.setCreated(LocalDateTime.now().minusDays(3));
        var pullLog2 = new ExerciseLog(9d, pullUp, webUser1);
        pullLog2.setCreated(LocalDateTime.now().minusDays(3));
        var cyclingLog1 = new ExerciseLog(10d, cycling, discordUser);
        exerciseLogRepository.saveAll(List.of(pullLog1, pullLog2, cyclingLog1));

        exerciseRecordRepository.save(new ExerciseRecord(discordUser, pullUp, 5d));
        exerciseRecordRepository.save(new ExerciseRecord(webUser1, pullUp, 9d));
        exerciseRecordRepository.save(new ExerciseRecord(discordUser, cycling, 10d));

        goalRepository.save(new Goal(60d, benchpress, discordUser));
        goalRepository.save(new Goal(10d, pullUp, discordUser));

        log.info("Finished dummy-data initialization");
    }
}
