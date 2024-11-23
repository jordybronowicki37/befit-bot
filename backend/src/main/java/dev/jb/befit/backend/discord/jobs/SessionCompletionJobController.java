package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.data.models.ExerciseSessionStatus;
import dev.jb.befit.backend.service.ExerciseSessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCompletionJobController {
    private final ExerciseSessionService exerciseSessionService;

    @Transactional
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void finalizeSessions() {
        var sessions = exerciseSessionService.getAllActiveAndOutdated();

        sessions.forEach(session -> {
            if (!session.getStatus().equals(ExerciseSessionStatus.ACTIVE)) return;
            exerciseSessionService.updateStatus(session.getUser(), session.getId(), ExerciseSessionStatus.FINISHED);
            log.info("Completed session {}", session.getId());
        });
    }
}
