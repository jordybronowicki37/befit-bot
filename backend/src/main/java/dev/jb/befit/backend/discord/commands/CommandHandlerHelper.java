package dev.jb.befit.backend.discord.commands;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class CommandHandlerHelper {
    private final ResourceLoader resourceLoader;
    private final JacksonResources d4jMapper = JacksonResources.create();

    public ApplicationCommandRequest getCommandConfigFile(String fileName) throws IOException {
        var resource = resourceLoader.getResource("classpath:commands/" + fileName + ".json");
        return d4jMapper.getObjectMapper().readValue(resource.getContentAsString(StandardCharsets.UTF_8), ApplicationCommandRequest.class);
    }
}
