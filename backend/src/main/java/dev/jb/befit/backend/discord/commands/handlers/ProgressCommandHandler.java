package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.ProgressImageService;
import dev.jb.befit.backend.service.exceptions.NoProgressMadeException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressCommandHandler implements DiscordCommandHandler {
    private final ProgressImageService progressImageService;
    private final CommandHandlerHelper commandHandlerHelper;

    @Override
    public boolean validatePrefix(String message) {
        try {
            var commandData = commandHandlerHelper.getCommandConfigFile("progress");
            return message.startsWith(commandData.name());
        } catch (IOException e) {
            log.error("Error validating command prefix. A config file was not found.", e);
            return false;
        }
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent command) {
        var name = command.getOption("name").orElseThrow().getValue().orElseThrow().asString();
        var userId = command.getInteraction().getUser().getId();

        try {
            var progressImage = progressImageService.createProgressImage(userId, name);
            var inputStream = new FileInputStream(progressImage);
            return command.reply(
                    InteractionApplicationCommandCallbackSpec.builder()
                            .addFile("progress.png", inputStream)
                            .build()
            );
        } catch (FileNotFoundException e) {
            return Mono.empty();
        } catch (NoProgressMadeException e) {
            var embed = EmbedCreateSpec.builder()
                    .title("Something went wrong")
                    .description(e.getMessage())
                    .color(Color.RED)
                    .build();
            return command.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build());
        }
    }
}