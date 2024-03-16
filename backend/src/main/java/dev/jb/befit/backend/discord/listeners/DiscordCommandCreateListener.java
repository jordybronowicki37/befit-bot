package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.discord.commands.handlers.DiscordCommandHandler;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscordCommandCreateListener implements DiscordEventListener<ChatInputInteractionEvent> {
    private final List<DiscordCommandHandler> commandHandlers;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return Mono.just(event)
                .flatMap(command -> {
                    var handler = commandHandlers.stream().filter(commandHandler -> commandHandler.validatePrefix(command.getCommandName())).findFirst();
                    if (handler.isPresent()) return handler.get().handle(command);
                    return Mono.empty();
                })
                .then();
    }
}
