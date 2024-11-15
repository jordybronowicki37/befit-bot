package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseSessionService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.exceptions.SessionNotFoundException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionLastCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsViewLast;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var session = exerciseSessionService.getLastByUser(user).orElseThrow(SessionNotFoundException::new);
        var embed = SessionGetCommandHandler.getEmbed(session).build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
