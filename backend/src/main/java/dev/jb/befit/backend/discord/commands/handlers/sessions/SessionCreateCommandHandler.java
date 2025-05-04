package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseSessionService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCreateCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsCreate;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var channelId = event.getInteraction().getChannelId();
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var sessionName = CommandHandlerHelper.getOptionValue(subCommand, "name");
        var user = userService.getOrCreateDiscordUser(userId);
        var session = exerciseSessionService.create(user, sessionName, channelId);
        return event.editReply(SessionViewOneCommandHandler.getReplyEditSpec(session, SessionCommandType.CREATE, 0)).then();
    }
}
