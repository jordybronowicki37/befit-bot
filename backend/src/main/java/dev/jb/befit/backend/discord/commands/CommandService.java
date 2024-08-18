package dev.jb.befit.backend.discord.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandHandlerHelper commandHandlerHelper;
    private final GatewayDiscordClient discordClient;

    public void registerAllCommands() throws IOException {
        var commands = commandHandlerHelper.getAllCommandConfigFiles();
        commandHandlerHelper.addExerciseOptionsToCommands(commands);
        bulkUpdateCommands(commands);
    }

    public void updateCommandsWithExerciseNameOptions() throws IOException {
        // TODO only update relevant commands
        var commands = commandHandlerHelper.getAllCommandConfigFiles();
        commandHandlerHelper.addExerciseOptionsToCommands(commands);
        bulkUpdateCommands(commands);
    }

    private void bulkUpdateCommands(List<ApplicationCommandRequest> commands) throws IOException {
        final var restClient = discordClient.getRestClient();
        final var applicationService = restClient.getApplicationService();
        final var applicationId = restClient.getApplicationId().block();
        assert applicationId != null;

        restClient.getGuilds()
                .doOnNext(g -> log.debug("Updating commands for guild {}", g.name()))
                .flatMap(guild ->
                        applicationService.bulkOverwriteGuildApplicationCommand(applicationId, guild.id().asLong(), commands)
                                .doOnNext(cmd -> log.debug("Successfully updated guild command: {}", cmd.name()))
                                .doOnError(e -> log.error("Failed to update guild command: ", e))
                ).subscribe();
    }
}
