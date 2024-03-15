package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.service.MotivationalService;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
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
                .flatMap(c -> {
                    var quote = motivationalService.getRandomQuote();
                    var builder = EmbedCreateSpec.builder()
                            .title("Motivational quote")
                            .description(quote.message())
                            .footer(String.format("- %s", quote.author()), null)
                            .color(Color.GREEN);
                    return c.createMessage(builder.build());
                })
                .flatMap(c -> Mono.empty());
    }
}
