package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.MotivationalService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotivationCommandHandler implements DiscordCommandHandler {
    private final MotivationalService motivationalService;
    private final CommandHandlerHelper commandHandlerHelper;

    @Override
    public boolean validatePrefix(String message) {
        try {
            var commandData = commandHandlerHelper.getCommandConfigFile("motivation");
            return message.startsWith(commandData.name());
        } catch (IOException e) {
            log.error("Error validating command prefix. A config file was not found.", e);
            return false;
        }
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent command) {
        var quote = motivationalService.getRandomQuote();
        var embed = EmbedCreateSpec.builder()
                .title("Motivational quote")
                .description(quote.message())
                .footer(String.format("- %s", quote.author()), null)
                .color(Color.CYAN)
                .build();
        return command.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build());
    }
}
