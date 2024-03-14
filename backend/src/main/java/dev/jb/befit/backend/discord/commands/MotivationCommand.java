package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.service.MotivationalService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MotivationCommand implements DiscordCommandHandler {
    private final MotivationalService motivationalService;

    @Override
    public boolean validatePrefix(String message) {
        return message.startsWith("!motivation");
    }

    @Override
    public Mono<Void> handle(Message message) {
        return message.getChannel()
                .flatMap(channel -> channel.createMessage(motivationalService.getRandomQuote()))
                .flatMap(c -> Mono.empty());
    }
}
