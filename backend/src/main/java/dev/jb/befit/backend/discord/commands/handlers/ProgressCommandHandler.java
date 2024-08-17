package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.listeners.DiscordEventListener;
import dev.jb.befit.backend.service.ProgressImageService;
import dev.jb.befit.backend.service.exceptions.NoProgressMadeException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressCommandHandler implements DiscordEventListener<ChatInputInteractionEvent> {
    private final ProgressImageService progressImageService;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals("progress")) return Mono.empty();

        event.deferReply().block();

        var exerciseName = event.getOption("exercise-name").orElseThrow().getValue().orElseThrow().asString();
        var userId = event.getInteraction().getUser().getId();

        try {
            var progressImage = progressImageService.createProgressImage(userId, exerciseName);
            var inputStream = new FileInputStream(progressImage);
            return event.editReply(InteractionReplyEditSpec.builder().addFile("progress.png", inputStream).build()).then();
        } catch (FileNotFoundException e) {
            return Mono.empty();
        } catch (NoProgressMadeException e) {
            var embed = EmbedCreateSpec.builder()
                    .title("Something went wrong")
                    .description(e.getMessage())
                    .color(Color.RED)
                    .build();
            return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
        }
    }
}
