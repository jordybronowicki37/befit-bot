package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewExerciseCommandHandler implements DiscordCommandHandler {
    private final ExerciseTypeService exerciseService;
    private final CommandHandlerHelper commandHandlerHelper;

    @Override
    public boolean validatePrefix(String message) {
        try {
            var commandData = commandHandlerHelper.getCommandConfigFile("new-exercise-type");
            return message.startsWith(commandData.name());
        } catch (IOException e) {
            log.error("Error validating command prefix. A config file was not found.", e);
            return false;
        }
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent command) {
        var name = command.getOption("name").orElseThrow().getValue().orElseThrow().asString();
        var exercise = exerciseService.create(name);
        var builder = EmbedCreateSpec.builder()
                .title("Your new exercise")
                .description(String.format("#%d Exercise%n%s", exercise.getId(), exercise.getName()))
                .color(Color.ORANGE);
        return command.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(builder.build()).build());
    }
}
