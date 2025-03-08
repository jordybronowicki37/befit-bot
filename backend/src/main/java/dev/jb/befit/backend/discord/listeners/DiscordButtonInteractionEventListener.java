package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.service.exceptions.MyException;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class DiscordButtonInteractionEventListener implements DiscordEventListener<ButtonInteractionEvent> {
    public abstract String getCommandNameFilter();

    public Class<ButtonInteractionEvent> getEventType() {
        return ButtonInteractionEvent.class;
    }

    public boolean acceptExecution(ButtonInteractionEvent event) {
        return event.getCustomId().startsWith(getCommandNameFilter()+'$');
    }

    public Mono<ButtonInteractionEvent> preExecute(ButtonInteractionEvent event) {
        return event.deferEdit().then(Mono.just(event));
    }

    public Mono<Void> handleError(Throwable error) {
        log.error("Unable to process: {}", getEventType().getSimpleName(), error);
        return Mono.empty();
    }

    public Mono<Void> replyWithErrorMessage(Throwable error, ButtonInteractionEvent initialEvent) {
        if (error instanceof MyException) {
            return DiscordChatInputInteractionEventListener.editReplyToError(initialEvent, "Something went wrong", error.getMessage());
        }
        else {
            log.error("An unhandled error has occurred", error);
            return DiscordChatInputInteractionEventListener.editReplyToError(initialEvent, "Something went wrong", "Please try again later.");
        }
    }
}
