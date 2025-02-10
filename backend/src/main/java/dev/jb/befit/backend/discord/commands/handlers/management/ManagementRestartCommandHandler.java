package dev.jb.befit.backend.discord.commands.handlers.management;

import dev.jb.befit.backend.BackendApplication;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagementRestartCommandHandler extends DiscordChatInputInteractionEventListener {
    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandManagementRestart;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.editReply(InteractionReplyEditSpec.builder().contentOrNull("Restarting...").build())
                .then(Mono.defer(() -> {
                    BackendApplication.restart();
                    return Mono.empty();
                }));
    }
}
