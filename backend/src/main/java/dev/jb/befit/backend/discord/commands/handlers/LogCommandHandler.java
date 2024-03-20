package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogCommandHandler implements DiscordCommandHandler {
    private final ExerciseLogService logService;
    private final UserService userService;
    private final CommandHandlerHelper commandHandlerHelper;

    @Override
    public boolean validatePrefix(String message) {
        try {
            var commandData = commandHandlerHelper.getCommandConfigFile("log");
            return message.startsWith(commandData.name());
        } catch (IOException e) {
            log.error("Error validating command prefix. A config file was not found.", e);
            return false;
        }
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent command) {
        var name = command.getOption("name").orElseThrow().getValue().orElseThrow().asString();
        var amount = Math.toIntExact(command.getOption("amount").orElseThrow().getValue().orElseThrow().asLong());
        var user = userService.getOrCreateDiscordUser(command.getInteraction().getUser().getId());
        var exerciseLog = logService.create(user, name, amount);
        var exersiceType = exerciseLog.getExerciseType();
        var allExerciseLogs = logService.getAllByUserIdAndExerciseName(user, name);

        var descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(String.format("Exercise: #%d %s\n", exersiceType.getId(), exersiceType.getName()));
        descriptionBuilder.append(String.format("Log: #%d\n", allExerciseLogs.size()));
        descriptionBuilder.append(String.format("Value: %d%s\n", exerciseLog.getAmount(), "KG"));
        if (allExerciseLogs.size() >= 2) {
            descriptionBuilder.append(String.format("Last: %d%s\n", allExerciseLogs.get(allExerciseLogs.size() - 2).getAmount(), "KG"));
        }

        var exerciseAmounts = new ArrayList<>(allExerciseLogs.stream().map(ExerciseLog::getAmount).toList());
        exerciseAmounts.remove(exerciseLog.getAmount());
        if (exerciseAmounts.stream().max(Integer::compareTo).orElse(0) < exerciseLog.getAmount()) {
            descriptionBuilder.append("\nNEW PR REACHED!");
        }

        var builder = EmbedCreateSpec.builder()
                .title("Logged workout")
                .description(descriptionBuilder.toString())
                .color(Color.GREEN);
        return command.reply(InteractionApplicationCommandCallbackSpec.builder().addEmbed(builder.build()).build());
    }
}
