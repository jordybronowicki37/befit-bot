package dev.jb.befit.backend.discord.commands.handlers;

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
public class AchievementsPaginationButtonHandler extends DiscordButtonInteractionEventListener {
    private final AchievementsCommandHandler achievementsCommandHandler;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandAchievements;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var userId = event.getMessage().get().getInteraction().get().getUser().getId();
        var page = Long.parseLong(event.getCustomId().split("\\$")[1]);
        return event.editReply(achievementsCommandHandler.getAchievementsEditSpec(userId, page)).then();
    }
}