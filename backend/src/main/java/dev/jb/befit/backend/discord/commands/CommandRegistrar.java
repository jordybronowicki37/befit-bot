package dev.jb.befit.backend.discord.commands;

import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandRegistrar implements CommandLineRunner {
    private final CommandHandlerHelper commandHandlerHelper;
    private final GatewayDiscordClient discordClient;

    protected void registerCommands() throws IOException {
        final var restClient = discordClient.getRestClient();
        final var applicationService = restClient.getApplicationService();
        final var applicationId = restClient.getApplicationId().block();
        assert applicationId != null;

        var commands = commandHandlerHelper.getAllCommandConfigFiles();
        commandHandlerHelper.addExerciseOptionsToCommands(commands);

        restClient.getGuilds()
                .doOnNext(g -> log.info("Registering commands for guild " + g.name()))
                .flatMap(guild ->
                        applicationService.bulkOverwriteGuildApplicationCommand(applicationId, guild.id().asLong(), commands)
                                .doOnNext(cmd -> log.info("Successfully registered guild command: " + cmd.name()))
                                .doOnError(e -> log.error("Failed to register guild command: ", e))
                ).subscribe();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Started registering all guild commands");
        registerCommands();
    }
}
