package dev.jb.befit.backend.discord.commands.handlers;

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
public class GetExercisesPaginationButtonHandler extends DiscordButtonInteractionEventListener {
    private final GetExercisesCommandHandler getExercisesCommandHandler;

    @Override
    public String getCommandNameFilter() {
        return "exercises view all";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var page = Integer.parseInt(event.getCustomId().split("\\$")[1]);
        return event.editReply(getExercisesCommandHandler.getExercisesEditSpec(page)).then();
    }
}
