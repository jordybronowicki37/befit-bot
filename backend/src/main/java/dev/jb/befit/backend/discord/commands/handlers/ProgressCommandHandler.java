package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ProgressImageService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ProgressImageService progressImageService;

    @Override
    public String getCommandNameFilter() {
        return "progress";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var exerciseName = event.getOption("exercise-name").orElseThrow().getValue().orElseThrow().asString();
        var userId = event.getInteraction().getUser().getId();

        try {
            var progressImage = progressImageService.createPersonalProgressChart(userId, exerciseName);
            var inputStream = new FileInputStream(progressImage);
            return event.editReply(InteractionReplyEditSpec.builder().addFile("progress.png", inputStream).build()).then();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
