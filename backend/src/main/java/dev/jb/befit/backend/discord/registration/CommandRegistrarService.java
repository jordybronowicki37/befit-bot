package dev.jb.befit.backend.discord.registration;

import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.MeasurementType;
import discord4j.common.JacksonResources;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.UserGuildData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND;
import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND_GROUP;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandRegistrarService {
    private static final String commandsFilesMatcher = "classpath:commands/*.json";
    private static final String commandsPath = "classpath:commands/%s.json";

    @Value("${discord.guilds.management}")
    private Long managementGuildId;

    private static Map<String, Long> commandIds = new HashMap<>();

    private final GatewayDiscordClient discordClient;
    private final ResourceLoader resourceLoader;
    private final JacksonResources d4jMapper = JacksonResources.create();

    public void registerAllCommands() throws IOException {
        final var guilds = getAllGuilds();
        final var managementGuilds = guilds.filter(g -> g.id().asLong() == managementGuildId);
        final var regularGuilds = guilds.filter(g -> g.id().asLong() != managementGuildId);

        var allCommands = getAllCommandConfigFiles();
        var globalCommands = allCommands.stream().filter(c -> !"management".equals(c.name())).toList();
        var managementCommands = allCommands.stream().filter(c -> "management".equals(c.name())).toList();

        bulkUpdateGlobalCommands(globalCommands);
        bulkUpdateGuildCommands(managementCommands, managementGuilds);
        // Reset guild commands for regular servers that were still using the old way of only using guild commands instead of global commands
        bulkUpdateGuildCommands(List.of(), regularGuilds);
    }

    private Flux<UserGuildData> getAllGuilds() {
        final var restClient = discordClient.getRestClient();
        return restClient.getGuilds();
    }

    private void bulkUpdateGuildCommands(List<ApplicationCommandRequest> commands, Flux<UserGuildData> guilds) {
        final var restClient = discordClient.getRestClient();
        final var applicationService = restClient.getApplicationService();
        final var applicationId = restClient.getApplicationId().block();
        assert applicationId != null;

        guilds
                .doOnNext(guild -> log.debug("Updating commands for guild {}", guild.name()))
                .flatMap(guild ->
                        applicationService.bulkOverwriteGuildApplicationCommand(applicationId, guild.id().asLong(), commands)
                                .doOnNext(cmd -> log.debug("Successfully updated guild command: {}", cmd.name()))
                                .doOnError(e -> log.error("Failed to update guild command: ", e))
                ).subscribe();
    }

    private void bulkUpdateGlobalCommands(List<ApplicationCommandRequest> commands) {
        final var restClient = discordClient.getRestClient();
        final var applicationService = restClient.getApplicationService();
        final var applicationId = restClient.getApplicationId().block();
        assert applicationId != null;

        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands)
                .doOnNext(cmd -> {
                    log.debug("Successfully updated global command: {}", cmd.name());
                    commandIds.put(cmd.name(), cmd.id().asLong());
                })
                .doOnError(e -> log.error("Failed to update global command: ", e))
                .subscribe();
    }

    public static Long getCommandId(String commandName) {
        var parentName = commandName.split(" ")[0];
        if (commandIds.containsKey(parentName)) return commandIds.get(parentName);
        return null;
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

    private void applyMeasurementsOptions(List<ApplicationCommandRequest> commands) {
        var measurementTypes = MeasurementType.values();
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
