package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.discord.commands.handlers.sessions.SessionRateButtonHandler;
import dev.jb.befit.backend.service.ExerciseSessionService;
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

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionExtendButtonRemovalJobController {
    private final GatewayDiscordClient client;
    private final ExerciseSessionService exerciseSessionService;

    @Transactional
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void checkLogsForUndoButtonRemoval() {
        var sessions = exerciseSessionService.getAllOutdatedExtends();
        Flux.fromIterable(sessions)
                .flatMap(session -> Mono.just(session)
                        .flatMap(s -> client.getMessageById(s.getDiscordChannelId(), s.getDiscordMessageId())
                                .flatMap(m -> {
                                    exerciseSessionService.updateMessage(session.getUser(), session.getId(), null);
                                    var newMessage = MessageEditSpec
                                            .builder()
                                            .addComponent(SessionRateButtonHandler.getRatingRow(session))
                                            .build();
                                    return m.edit(newMessage);
                                })
                        )
                        .doOnError(ClientException.class, e -> {
                            if (e.getStatus().equals(HttpResponseStatus.NOT_FOUND)) {
                                exerciseSessionService.updateMessage(session.getUser(), session.getId(), null);
                            }
                        })
                )
                .subscribe();
    }
}
