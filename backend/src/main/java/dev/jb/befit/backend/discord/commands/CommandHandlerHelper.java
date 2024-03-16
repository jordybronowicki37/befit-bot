package dev.jb.befit.backend.discord.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@RequiredArgsConstructor
public class CommandHandlerHelper {
    private final ResourceLoader resourceLoader;

    public CommandData getCommandConfigFile(String fileName) throws IOException {
        var resource = resourceLoader.getResource("classpath:commands/" + fileName + ".json");
        var objectMapper = new ObjectMapper();
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

    public record CommandData(String name, String description) {}
}
