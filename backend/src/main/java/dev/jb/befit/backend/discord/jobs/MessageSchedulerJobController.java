package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.data.models.ScheduledJob;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class MessageSchedulerJobController {
    private final TaskScheduler taskScheduler;
    private final ScheduledJobGymReminderService scheduledJobGymReminderService;
    private final ScheduledJobMotivationalQuoteService scheduledJobMotivationalQuoteService;

    @Getter
    private final Map<ScheduledJob, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void scheduleJob(ScheduledJob job) {
        if (scheduledTasks.containsKey(job)) {
            // Cancel the existing job if already scheduled
            scheduledTasks.get(job).cancel(false);
        }

        // Schedule the task
        var cronTrigger = new CronTrigger(job.getCronExpression(), job.getTimeZone());
        var scheduledFuture = taskScheduler.schedule(getTask(job), cronTrigger);
        scheduledTasks.put(job, scheduledFuture);
    }

    public void removeJob(Long jobId) {
        var foundJob = scheduledTasks.keySet().stream().filter(v -> v.getId().equals(jobId)).findFirst();
        if (foundJob.isPresent()) {
            scheduledTasks.get(foundJob.get()).cancel(false);
            scheduledTasks.remove(foundJob.get());
        }
    }

    private Runnable getTask(ScheduledJob job) {
        switch (job.getType()) {
            case GYM_REMINDER -> {
                return () -> scheduledJobGymReminderService.publishGymReminderQuote(job.getChannelId());
            }
            case MOTIVATIONAL_MESSAGE -> {
                return () -> scheduledJobMotivationalQuoteService.publishMotivationalQuote(job.getChannelId());
            }
        }
        return () -> {};
    }
}
