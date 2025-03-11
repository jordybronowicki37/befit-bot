package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.service.ExerciseLogService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageEditSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogUndoButtonRemovalJobController {
    private final GatewayDiscordClient client;
    private final ExerciseLogService exerciseLogService;

    @Scheduled(cron = "0 * * * * *")
    public void checkLogsForUndoButtonRemoval() {
        var logs = exerciseLogService.getAllExpiredUndoSchedules();
        Flux.fromIterable(logs)
                .flatMap(l -> client.getMessageById(l.getChannelId(), l.getMessageId())
                        .flatMap(m -> {
                            exerciseLogService.removeUndoExpiry(l.getUser(), l.getId());
                            return m.edit(MessageEditSpec.builder().components(List.of()).build());
                        })
                )
                .subscribe();
    }
}
