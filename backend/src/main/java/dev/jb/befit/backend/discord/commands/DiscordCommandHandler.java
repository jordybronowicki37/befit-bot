package dev.jb.befit.backend.discord.commands;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public interface DiscordCommandHandler {
    boolean validatePrefix(String message);
    Mono<Void> handle(Message message);
}
