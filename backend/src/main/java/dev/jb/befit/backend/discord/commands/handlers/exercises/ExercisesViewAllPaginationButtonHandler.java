package dev.jb.befit.backend.discord.commands.handlers.exercises;

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
public class ExercisesViewAllPaginationButtonHandler extends DiscordButtonInteractionEventListener {
    private final ExercisesViewAllCommandHandler exercisesViewAllCommandHandler;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandExercisesViewAll;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var page = Integer.parseInt(event.getCustomId().split("\\$")[2]);
        return event.editReply(exercisesViewAllCommandHandler.getReplyEditSpec(page)).then();
    }
}
