package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.MeasurementTypes;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

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
        var command = d4jMapper.getObjectMapper().readValue(resource.getContentAsString(StandardCharsets.UTF_8), ApplicationCommandRequest.class);
        applyAllOptions(List.of(command));
        return command;
    }

    public List<ApplicationCommandRequest> getAllCommandConfigFiles() throws IOException {
        var commandFiles = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(commandsFilesMatcher);
        var commands = Arrays.stream(commandFiles).map(r -> {
            try {
                return d4jMapper.getObjectMapper().readValue(r.getContentAsString(StandardCharsets.UTF_8), ApplicationCommandRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        applyAllOptions(commands);
        return commands;
    }

    private void applyAllOptions(List<ApplicationCommandRequest> commands) {
        applyExerciseOptions(commands);
        applyMeasurementsOptions(commands);
        applyGoalDirectionOptions(commands);
    }

    private void applyGenericOptions(List<ApplicationCommandRequest> commands, String key, Consumer<List<ApplicationCommandOptionChoiceData>> choicesEditingConsumer) {
        for (var command : commands) {
            if (command.options().isAbsent()) continue;
            command.options().get().stream()
                    .flatMap(option -> {
                        // Check for subcommand and extract options
                        if (option.type() == 1 && !option.options().isAbsent())
                            return option.options().get().stream();
                        // Check for subcommand-group with subcommands and extract options
                        if (option.type() == 2 && !option.options().isAbsent())
                            return option.options().get().stream().flatMap(subgroup -> subgroup.options().get().stream());
                        return Stream.of(option);
                    })
                    .filter(option -> key.equals(option.name()))
                    .forEach(option -> {
                        log.info("applying {} choices to command: {}", key, command.name());
                        if (option.choices().isAbsent()) return;
                        var choices = option.choices().get();
                        choicesEditingConsumer.accept(choices);
                    });
        }
    }

    private void applyExerciseOptions(List<ApplicationCommandRequest> commands) {
        var exerciseTypes = exerciseTypeService.getAll();
        log.debug("Adding {} exercise options to commands", exerciseTypes.size());

        applyGenericOptions(commands, "exercise-name", choices -> {
            for (var exerciseType : exerciseTypes) {
                choices.add(ApplicationCommandOptionChoiceData.builder()
                        .name(exerciseType.getName())
                        .value(exerciseType.getName())
                        .build()
                );
            }
        });
    }

    private void applyMeasurementsOptions(List<ApplicationCommandRequest> commands) {
        applyGenericOptions(commands, "measurement-type", choices -> {
            for (var measurementType : MeasurementTypes.values()) {
                choices.add(ApplicationCommandOptionChoiceData.builder()
                        .name(measurementType.getLongName())
                        .value(measurementType.name())
                        .build()
                );
            }
        });
    }

    private void applyGoalDirectionOptions(List<ApplicationCommandRequest> commands) {
        applyGenericOptions(commands, "goal-direction", choices -> {
            for (var goalDirection : GoalDirection.values()) {
                choices.add(ApplicationCommandOptionChoiceData.builder()
                        .name(goalDirection.name().toLowerCase())
                        .value(goalDirection.name())
                        .build()
                );
            }
        });
    }
}
