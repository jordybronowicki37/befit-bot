package dev.jb.befit.backend.discord;

import dev.jb.befit.backend.discord.commands.DiscordCommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscordMessageCreateListener implements DiscordEventListener<MessageCreateEvent> {
    private final List<DiscordCommandHandler> commandHandlers;

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.just(event.getMessage())
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .flatMap(message -> {
                    var handler = commandHandlers.stream().filter(commandHandler -> commandHandler.validatePrefix(message.getContent())).findFirst();
                    if (handler.isPresent()) return handler.get().handle(message);
                    return message.getChannel();
                })
                .then();
    }
}
