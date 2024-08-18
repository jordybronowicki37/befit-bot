package dev.jb.befit.backend.discord.listeners;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface DiscordEventListener<T extends Event> {
    Class<T> getEventType();
    boolean acceptExecution(T event);
    Mono<T> preExecute(T event);
    Mono<Void> execute(T event);
    Mono<Void> handleError(Throwable error);
    Mono<Void> replyWithErrorMessage(Throwable error, T initialEvent);
}
