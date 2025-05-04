package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordButtonInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseSessionService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.exceptions.SessionNotFoundException;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionViewOnePaginationButtonHandler extends DiscordButtonInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public boolean acceptExecution(ButtonInteractionEvent event) {
        return SessionCommandsConstants.sessionSingleCommands.stream().anyMatch(commandName -> event.getCustomId().startsWith(commandName+'$'));
    }

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsViewOne;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var userId = event.getMessage().get().getInteraction().get().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var customIdSplit = event.getCustomId().split("\\$");
        var page = Integer.parseInt(customIdSplit[2]);
        var sessionId = Long.parseLong(customIdSplit[3]);
        var session = exerciseSessionService.getByUserAndId(user, sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        var commandName = customIdSplit[0];
        var commandType = switch (commandName) {
            case CommandConstants.CommandSessionsCreate -> SessionCommandType.CREATE;
            case CommandConstants.CommandSessionsStop -> SessionCommandType.STOP;
            default -> SessionCommandType.VIEW;
        };
        return event.editReply(SessionViewOneCommandHandler.getReplyEditSpec(session, commandType, page)).then();
    }
}
