package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordButtonInteractionEventListener;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitsViewAllPaginationButtonHandler extends DiscordButtonInteractionEventListener {
    private final HabitsViewAllCommandHandler habitsViewAllCommandHandler;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsViewAll;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var userId = event.getMessage().get().getInteraction().get().getUser().getId();
        var commandSplit = event.getCustomId().split("\\$");
        var page = Integer.parseInt(commandSplit[2]);
        var habitTimeRangeRaw = commandSplit[1];
        HabitTimeRange habitTimeRange = null;
        if (!Objects.equals(habitTimeRangeRaw, "null")) {
            habitTimeRange = HabitTimeRange.valueOf(habitTimeRangeRaw);
        }
        return event.editReply(habitsViewAllCommandHandler.getReplyEditSpec(userId, page, habitTimeRange)).then();
    }
}
