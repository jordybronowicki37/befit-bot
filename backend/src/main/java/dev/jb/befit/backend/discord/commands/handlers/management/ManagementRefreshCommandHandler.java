package dev.jb.befit.backend.discord.commands.handlers.management;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.discord.registration.CommandRegistrarService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagementRefreshCommandHandler extends DiscordChatInputInteractionEventListener {
    private final CommandRegistrarService commandRegistrarService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandManagementRefreshCommands;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        try {
            commandRegistrarService.registerAllCommands();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return event.editReply(InteractionReplyEditSpec.builder().contentOrNull("Refreshing...").build()).then();
    }
}
