package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.service.MotivationalService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MotivationCommandHandler implements DiscordCommandHandler {
    private final MotivationalService motivationalService;

    @Override
    public boolean validatePrefix(String message) {
        return message.startsWith("motivation");
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent command) {
        var quote = motivationalService.getRandomQuote();
        var builder = EmbedCreateSpec.builder()
                .title("Motivational quote")
                .description(quote.message())
                .footer(String.format("- %s", quote.author()), null)
                .color(Color.GREEN);
        return command.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(builder.build()).build());
    }
}
