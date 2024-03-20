package dev.jb.befit.backend.discord.commands;

import discord4j.common.JacksonResources;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandRegistrar implements CommandLineRunner {
    private static final String commandsFilesMatcher = "classpath:commands/*.json";

    private final ResourceLoader resourceLoader;
    private final GatewayDiscordClient discordClient;

    protected void registerCommands() throws IOException {
        final var restClient = discordClient.getRestClient();
        final var d4jMapper = JacksonResources.create();
        final var applicationService = restClient.getApplicationService();
        final var applicationId = restClient.getApplicationId().block();
        assert applicationId != null;

        // Get our commands json from resources as command data
        var commandFiles = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(commandsFilesMatcher);
        var commands = Arrays.stream(commandFiles).map(r -> {
            try {
                return d4jMapper.getObjectMapper().readValue(r.getContentAsString(StandardCharsets.UTF_8), ApplicationCommandRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

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
