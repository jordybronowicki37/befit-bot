package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.DiscordUser;
import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.ExerciseType;
import discord4j.common.util.Snowflake;
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
    private final DiscordUserRepository discordUserRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    @Value("${discord.dummy-user-id}")
    private String userId;

    @Override
    public void run(String... args) {
        log.info("Started dummy-data initialization");
        var user = discordUserRepository.save(new DiscordUser(Snowflake.of(userId)));
        var benchpress = exerciseTypeRepository.save(new ExerciseType("benchpress", "KG"));

        var log1 = new ExerciseLog(20, benchpress, user);
        log1.setCreated(LocalDateTime.now().minusDays(8));
        var log2 = new ExerciseLog(23, benchpress, user);
        log2.setCreated(LocalDateTime.now().minusDays(5));
        var log3 = new ExerciseLog(31, benchpress, user);
        log3.setCreated(LocalDateTime.now().minusDays(3));
        var log4 = new ExerciseLog(45, benchpress, user);
        log4.setCreated(LocalDateTime.now().minusDays(1));
        exerciseLogRepository.saveAll(List.of(log1, log2, log3, log4));
        log.info("Finished dummy-data initialization");
    }
}
