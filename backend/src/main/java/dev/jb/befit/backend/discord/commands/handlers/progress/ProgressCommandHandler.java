package dev.jb.befit.backend.discord.commands.handlers.progress;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.commands.exceptions.InvalidValueException;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.visuals.ProgressImageService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ProgressImageService progressImageService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandProgress;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var exerciseName = CommandHandlerHelper.getOptionValue(event, CommandConstants.AutoCompletePropExerciseName);
        var viewMode = CommandHandlerHelper.getOptionalOptionValue(event, "view-mode", "own");

        try {
            File progressImage;
            if (viewMode.equals("own") || viewMode.isEmpty()) {
                var userId = CommandHandlerHelper.getDiscordUserId(event);
                progressImage = progressImageService.createPersonalProgressChart(userId, exerciseName);
            }
            else if (viewMode.equals("all")) {
                progressImage = progressImageService.createGlobalProgressChart(exerciseName);
            }
            else {
                throw new InvalidValueException("view-mode", viewMode);
            }
            var inputStream = new FileInputStream(progressImage);
            return event.editReply(InteractionReplyEditSpec.builder().addFile("progress.png", inputStream).build()).then();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
