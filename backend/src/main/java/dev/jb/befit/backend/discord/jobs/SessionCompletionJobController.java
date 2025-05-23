package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.ExerciseSessionStatus;
import dev.jb.befit.backend.discord.commands.handlers.sessions.SessionRateButtonHandler;
import dev.jb.befit.backend.service.ExerciseSessionService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.http.client.ClientException;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCompletionJobController {
    private final ExerciseSessionService exerciseSessionService;
    private final GatewayDiscordClient client;

    @Transactional
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void finalizeSessions() {
        var mono = Mono.empty().then();
        var sessions = exerciseSessionService.getAllActiveAndOutdated();

        for (var session : sessions) {
            if (session.getStatus().equals(ExerciseSessionStatus.ACTIVE)) {
                exerciseSessionService.updateStatus(session.getUser(), session.getId(), ExerciseSessionStatus.FINISHED);
                log.debug("Completed session {}", session.getId());
            }

            mono = sendRatingReport(mono, session);
        }

        mono.subscribe();
    }

    public Mono<Void> sendRatingReport(Mono<Void> mono, ExerciseSession session) {
        if (session.getExerciseLogs().isEmpty()) return mono;
        var channelId = session.getDiscordChannelId();
        if (channelId == null) return mono;

        return mono.then(client.getChannelById(channelId)
                .flatMap(channel -> {
                            log.debug("Sending session rating form to channel {}", channelId);
                            var message = MessageCreateSpec.builder()
                                    .content("# :notepad_spiral: Rate your session\nHow was your session? Are you satisfied with the result? Give your session a rating.")
                                    .addComponent(SessionRateButtonHandler.getRatingRow(session))
                                    .addComponent(SessionRateButtonHandler.getExtendSessionRow(session))
                                    .addEmbed(SessionRateButtonHandler.getSessionRecap(session));
                            return channel
                                    .getRestChannel()
                                    .createMessage(message.build().asRequest())
                                    .flatMap(m -> Mono.just(exerciseSessionService.updateMessage(session.getUser(), session.getId(), Snowflake.of(m.id()))));
                        }
                )
                .doOnError(ClientException.class, e -> {
                    if (e.getStatus().equals(HttpResponseStatus.NOT_FOUND)) {
                        exerciseSessionService.updateChannel(session.getUser(), session.getId(), null);
                    }
                })
                .then()
        );
    }
}
