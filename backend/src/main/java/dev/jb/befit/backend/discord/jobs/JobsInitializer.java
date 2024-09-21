package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobsInitializer implements CommandLineRunner {
    private final JobScheduler jobScheduler;
    private final ScheduledJobService scheduledJobService;

    @Override
    public void run(String... args) {
        var jobs = scheduledJobService.getAll();
        jobs.forEach(jobScheduler::scheduleJob);
    }
}
