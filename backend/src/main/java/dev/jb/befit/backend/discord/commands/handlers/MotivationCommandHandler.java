package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.MotivationalService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotivationCommandHandler extends DiscordChatInputInteractionEventListener {
    private final MotivationalService motivationalService;

    @Override
    public String getCommandNameFilter() {
        return "motivation";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var quote = motivationalService.getRandomQuote();
        var embed = EmbedCreateSpec.builder()
                .title("Motivational quote")
                .description(quote.message())
                .footer(String.format("- %s", quote.author()), null)
                .color(Color.CYAN)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
