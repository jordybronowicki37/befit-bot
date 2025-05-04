package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class DiscordChatInputAutoCompleteEventListener implements DiscordEventListener<ChatInputAutoCompleteEvent> {
    public abstract String getCommandNameFilter();

    public Class<ChatInputAutoCompleteEvent> getEventType() {
        return ChatInputAutoCompleteEvent.class;
    }

    public boolean acceptExecution(ChatInputAutoCompleteEvent event) {
        return event.getFocusedOption().getName().equals(getCommandNameFilter());
    }

    public void logExecution(ChatInputAutoCompleteEvent event) {
        log.info(
                "Executing autocomplete handler: {}, discord user id: {}, username: {}, filter: {}",
                getCommandNameFilter(),
                CommandHandlerHelper.getDiscordUserId(event).asString(),
                CommandHandlerHelper.getDiscordUserName(event),
                CommandHandlerHelper.getAutocompleteOptionFilter(event)
        );
    }

    public Mono<ChatInputAutoCompleteEvent> preExecute(ChatInputAutoCompleteEvent event) {
        return Mono.just(event);
    }

    public Mono<Void> handleError(Throwable error) {
        log.error("Unable to process: {}", getEventType().getSimpleName(), error);
        return Mono.empty();
    }

    public Mono<Void> replyWithErrorMessage(Throwable error, ChatInputAutoCompleteEvent initialEvent) {
        return Mono.empty();
    }
}
