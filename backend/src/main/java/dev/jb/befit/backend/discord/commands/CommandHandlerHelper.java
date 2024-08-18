package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandHandlerHelper {
    private static final String commandsFilesMatcher = "classpath:commands/*.json";
    private static final String commandsPath = "classpath:commands/";

    private final ResourceLoader resourceLoader;
    private final JacksonResources d4jMapper = JacksonResources.create();
    private final ExerciseTypeService exerciseTypeService;

    public ApplicationCommandRequest getCommandConfigFile(String fileName) throws IOException {
        var resource = resourceLoader.getResource(commandsPath + fileName + ".json");
        return d4jMapper.getObjectMapper().readValue(resource.getContentAsString(StandardCharsets.UTF_8), ApplicationCommandRequest.class);
    }

    public List<ApplicationCommandRequest> getAllCommandConfigFiles() throws IOException {
        var commandFiles = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(commandsFilesMatcher);
        return Arrays.stream(commandFiles).map(r -> {
            try {
                return d4jMapper.getObjectMapper().readValue(r.getContentAsString(StandardCharsets.UTF_8), ApplicationCommandRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    public void addExerciseOptionsToCommands(List<ApplicationCommandRequest> commands) {
        var exerciseTypes = exerciseTypeService.getAll();
        log.debug("Adding {} exercise options to commands", exerciseTypes.size());

        for (var command : commands) {
            command.options().get().stream()
                    .filter(option -> "exercise-name".equals(option.name()))
                    .forEach(option -> {
                        var choices = option.choices().get();
                        for (var exerciseType : exerciseTypes) {
                            choices.add(ApplicationCommandOptionChoiceData.builder()
                                    .name(exerciseType.getName())
                                    .value(exerciseType.getName())
                                    .build()
                            );
                        }
                    });
        }
    }
}
