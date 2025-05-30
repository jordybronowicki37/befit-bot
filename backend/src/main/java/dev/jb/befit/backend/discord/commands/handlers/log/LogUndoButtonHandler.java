package dev.jb.befit.backend.discord.commands.handlers.log;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordButtonInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogUndoButtonHandler extends DiscordButtonInteractionEventListener {
    private final ExerciseLogService exerciseLogService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandLogUndo;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var logId = Long.parseLong(event.getCustomId().split("\\$")[2]);
        var log = exerciseLogService.undoLogCreation(user, logId);

        // TODO give some more info on the log being removed
        var replySpec = InteractionReplyEditSpec.builder();
        replySpec.contentOrNull("Log successfully removed");
        replySpec.componentsOrNull(null);
        replySpec.embedsOrNull(null);

        return event
                .editReply(replySpec.build())
                .flatMap(m -> m.edit().withAttachments())
                .then();
    }
}
