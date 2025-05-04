package dev.jb.befit.backend.discord;

import dev.jb.befit.backend.discord.listeners.DiscordEventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListenerRegistrar <T extends Event> implements CommandLineRunner {
    public final GatewayDiscordClient discordClient;
    public final List<DiscordEventListener<T>> eventListeners;

    @Override
    public void run(String... args) {
        log.info("Registering listeners");
        for(DiscordEventListener<T> listener : eventListeners) {
            discordClient.on(listener.getEventType())
                    .filter(listener::acceptExecution)
                    .doOnNext(listener::logExecution)
                    .flatMap(listener::preExecute)
                    .flatMap(e -> Mono.defer(() -> listener.execute(e)).onErrorResume(t -> listener.replyWithErrorMessage(t, e)))
                    .onErrorResume(listener::handleError)
                    .subscribe();
        }
    }
}
