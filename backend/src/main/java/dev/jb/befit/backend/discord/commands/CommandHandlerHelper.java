package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.MeasurementTypes;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.common.JacksonResources;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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

import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND;
import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND_GROUP;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandHandlerHelper {
    private static final String commandsFilesMatcher = "classpath:commands/*.json";
    private static final String commandsPath = "classpath:commands/%s.json";

    private final ResourceLoader resourceLoader;
    private final JacksonResources d4jMapper = JacksonResources.create();
    private final ExerciseTypeService exerciseTypeService;

    public static boolean checkCommandName(ChatInputInteractionEvent event, String commandName) {
        var actualCommandNameBuilder = new StringBuilder();
        actualCommandNameBuilder.append(event.getCommandName());

        if (!event.getOptions().isEmpty()) {
            var nestedOption = event.getOptions().get(0);
            var nestedOptionIsSubCommand = nestedOption.getType().equals(SUB_COMMAND);
            var nestedOptionIsSubCommandGroup = nestedOption.getType().equals(SUB_COMMAND_GROUP);

            if (nestedOptionIsSubCommand || nestedOptionIsSubCommandGroup) {
                actualCommandNameBuilder.append(" ").append(nestedOption.getName());
            }

            if (nestedOptionIsSubCommandGroup && !nestedOption.getOptions().isEmpty()) {
                actualCommandNameBuilder.append(" ").append(nestedOption.getOptions().get(0).getName());
            }
        }

        return actualCommandNameBuilder.toString().equals(commandName);
    }

    public ApplicationCommandRequest getCommandConfigFile(String fileName) throws IOException {
        var resource = resourceLoader.getResource(String.format(commandsPath, fileName));
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
                        if (option.type() == SUB_COMMAND.getValue() && !option.options().isAbsent())
                            return option.options().toOptional().orElse(List.of()).stream();
                        // Check for subcommand-group with subcommands and extract options
                        if (option.type() == SUB_COMMAND_GROUP.getValue() && !option.options().isAbsent())
                            return option.options().get().stream().flatMap(subgroup -> subgroup.options().toOptional().orElse(List.of()).stream());
                        return Stream.of(option);
                    })
                    .filter(option -> key.equals(option.name()))
                    .forEach(option -> {
                        log.debug("applying {} choices to command: {}", key, command.name());
                        if (option.choices().isAbsent()) return;
                        var choices = option.choices().get();
                        choicesEditingConsumer.accept(choices);
                    });
        }
    }

    private void applyExerciseOptions(List<ApplicationCommandRequest> commands) {
        var exerciseTypes = exerciseTypeService.getAll();
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
        var measurementTypes = MeasurementTypes.values();
        applyGenericOptions(commands, "measurement-type", choices -> {
            for (var measurementType : measurementTypes) {
                choices.add(ApplicationCommandOptionChoiceData.builder()
                        .name(measurementType.getLongName())
                        .value(measurementType.name())
                        .build()
                );
            }
        });
    }

    private void applyGoalDirectionOptions(List<ApplicationCommandRequest> commands) {
        var goalDirections = GoalDirection.values();
        applyGenericOptions(commands, "goal-direction", choices -> {
            for (var goalDirection : goalDirections) {
                choices.add(ApplicationCommandOptionChoiceData.builder()
                        .name(goalDirection.name().toLowerCase())
                        .value(goalDirection.name())
                        .build()
                );
            }
        });
    }
}
