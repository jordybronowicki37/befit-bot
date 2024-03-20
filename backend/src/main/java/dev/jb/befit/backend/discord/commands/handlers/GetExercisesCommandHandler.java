package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
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
public class GetExercisesCommandHandler implements DiscordCommandHandler {
    private final ExerciseTypeService exerciseService;
    private final CommandHandlerHelper commandHandlerHelper;

    @Override
    public boolean validatePrefix(String message) {
        try {
            var commandData = commandHandlerHelper.getCommandConfigFile("get-exercise-types");
            return message.startsWith(commandData.name());
        } catch (IOException e) {
            log.error("Error validating command prefix. A config file was not found.", e);
            return false;
        }
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent command) {
        var exercises = exerciseService.getAll();
        var builder = EmbedCreateSpec.builder()
                .title("All Exercises")
                .fields(exercises
                        .stream()
                        .map(exercise -> EmbedCreateFields.Field.of(
                                String.format("#%d Exercise", exercise.getId()),
                                String.format("%s - %s", exercise.getName(), exercise.getMeasurementType()),
                                false)
                        )
                        .toList())
                .color(Color.BLUE);
        return command.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(builder.build()).build());
    }
}
