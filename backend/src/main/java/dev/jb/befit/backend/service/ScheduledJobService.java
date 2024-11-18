package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ScheduledJobRepository;
import dev.jb.befit.backend.data.models.ScheduledJob;
import dev.jb.befit.backend.discord.jobs.MessageSchedulerJobController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobService {
    private final ScheduledJobRepository scheduledJobRepository;
    private final MessageSchedulerJobController messageSchedulerJobController;

    public List<ScheduledJob> getAll() {
        return scheduledJobRepository.findAll();
    }

    public ScheduledJob create(ScheduledJob scheduledJob) {
        var savedJob = scheduledJobRepository.save(scheduledJob);
        messageSchedulerJobController.scheduleJob(savedJob);
        return savedJob;
    }

    public void delete(Long id) {
        messageSchedulerJobController.removeJob(id);
        scheduledJobRepository.deleteById(id);
    }
}
