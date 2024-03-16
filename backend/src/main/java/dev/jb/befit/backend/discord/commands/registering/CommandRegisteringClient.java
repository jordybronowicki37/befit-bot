package dev.jb.befit.backend.discord.commands.registering;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

@Slf4j
@RequiredArgsConstructor
public abstract class CommandRegisteringClient implements CommandLineRunner {
    protected final GatewayDiscordClient client;

    abstract ApplicationCommandRequest getCommand();

    @Override
    public void run(String... args) {
        log.info("Registering slash command using: {}", this.getClass().getSimpleName());
        var applicationId = client.getSelf().block().getId();

        client.getRestClient().getApplicationService()
                .createGlobalApplicationCommand(applicationId.asLong(), getCommand())
                .subscribe();
    }
}
