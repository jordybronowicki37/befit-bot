package dev.jb.befit.backend.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface DiscordCommandHandler {
    boolean validatePrefix(String message);
    Mono<Void> handle(ChatInputInteractionEvent command);
}
