package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.service.ExerciseLogService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.http.client.ClientException;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogUndoButtonRemovalJobController {
    private final GatewayDiscordClient client;
    private final ExerciseLogService exerciseLogService;

    @Transactional
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void checkLogsForUndoButtonRemoval() {
        var logs = exerciseLogService.getAllExpiredUndoSchedules();
        Flux.fromIterable(logs)
                .flatMap(log -> Mono.just(log)
                        .flatMap(l -> client.getMessageById(l.getDiscordChannelId(), l.getDiscordMessageId())
                                .flatMap(m -> {
                                    exerciseLogService.removeUndoExpiry(log.getUser(), log.getId());
                                    return m.edit(MessageEditSpec.builder().components(List.of()).build());
                                })
                        )
                        .doOnError(ClientException.class, e -> {
                            if (e.getStatus().equals(HttpResponseStatus.NOT_FOUND)) {
                                exerciseLogService.removeUndoExpiry(log.getUser(), log.getId());
                            }
                        })
                )
                .subscribe();
    }
}
