package dev.jb.befit.backend.discord.commands.registering;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MotivationCommandRegisteringClient extends CommandRegisteringClient {
    public MotivationCommandRegisteringClient(GatewayDiscordClient client) {
        super(client);
    }

    @Override
    ApplicationCommandRequest getCommand() {
        return ApplicationCommandRequest.builder()
                .name("motivation")
                .description("Get a random motivational quote")
                .build();
    }
}
