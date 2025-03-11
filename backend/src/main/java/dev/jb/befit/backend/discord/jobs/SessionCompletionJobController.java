package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.ExerciseSessionStatus;
import dev.jb.befit.backend.discord.commands.handlers.sessions.SessionRateButtonHandler;
import dev.jb.befit.backend.service.ExerciseSessionService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageCreateSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCompletionJobController {
    private final ExerciseSessionService exerciseSessionService;
    private final GatewayDiscordClient client;

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void finalizeSessions() {
        var mono = Mono.empty();
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

    public Mono<Object> sendRatingReport(Mono<Object> mono, ExerciseSession session) {
        if (session.getExerciseLogs().isEmpty()) return mono;
        var channelId = session.getDiscordChannelId();
        if (channelId == null) return mono;

        return mono.then(client.getChannelById(channelId).flatMap(channel -> {
            log.debug("Sending session rating form to channel {}", channelId);
            var message = MessageCreateSpec.builder()
                    .content("# :notepad_spiral: Rate your session\nHow was your session? Are you satisfied with the result? Give your session a rating.")
                    .addComponent(SessionRateButtonHandler.getRatingRow(session))
                    .addEmbed(SessionRateButtonHandler.getSessionRecap(session));
            return channel.getRestChannel().createMessage(message.build().asRequest());
        }));
    }
}
