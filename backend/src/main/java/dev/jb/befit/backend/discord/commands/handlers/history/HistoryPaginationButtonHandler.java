package dev.jb.befit.backend.discord.commands.handlers.history;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordButtonInteractionEventListener;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryPaginationButtonHandler extends DiscordButtonInteractionEventListener {
    private final HistoryCommandHandler historyCommandHandler;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHistory;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var userId = event.getMessage().get().getInteraction().get().getUser().getId();
        var splitCommand = event.getCustomId().split("\\$");
        var page = Integer.parseInt(splitCommand[2]);
        var exerciseNameFilter = splitCommand[1];
        if (exerciseNameFilter.isEmpty()) exerciseNameFilter = null;
        return event.editReply(historyCommandHandler.getReplyEditSpec(userId, page, exerciseNameFilter)).then();
    }
}
