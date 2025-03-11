package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.service.ExerciseSessionService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageEditSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionExtendButtonRemovalJobController {
    private final GatewayDiscordClient client;
    private final ExerciseSessionService exerciseSessionService;

    @Scheduled(cron = "0 * * * * *")
    public void checkLogsForUndoButtonRemoval() {
        var sessions = exerciseSessionService.getAllOutdatedExtends();
        Flux.fromIterable(sessions)
                .flatMap(s -> client.getMessageById(s.getDiscordChannelId(), s.getDiscordMessageId())
                        .flatMap(m -> {
                            exerciseSessionService.updateMessage(s.getUser(), s.getId(), null);
                            return m.edit(MessageEditSpec.builder().addComponent(m.getComponents().get(0)).build());
                        })
                )
                .subscribe();
    }
}
