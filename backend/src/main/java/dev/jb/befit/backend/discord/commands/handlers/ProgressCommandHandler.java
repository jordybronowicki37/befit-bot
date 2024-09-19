package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.exceptions.InvalidValueException;
import dev.jb.befit.backend.discord.commands.exceptions.OptionNotFoundException;
import dev.jb.befit.backend.discord.commands.exceptions.ValueNotFoundException;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ProgressImageService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
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
        return "progress";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var exerciseNameOption = event.getOption("exercise-name").orElseThrow(() -> new OptionNotFoundException("exercise-name"));
        var exerciseName = exerciseNameOption.getValue().orElseThrow(() -> new ValueNotFoundException("exercise-name")).asString();
        var viewMode = event.getOption("view-mode").map(v -> v.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse("own")).orElse("own");

        try {
            File progressImage;
            if (viewMode.equals("own") || viewMode.isEmpty()) {
                var userId = event.getInteraction().getUser().getId();
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
