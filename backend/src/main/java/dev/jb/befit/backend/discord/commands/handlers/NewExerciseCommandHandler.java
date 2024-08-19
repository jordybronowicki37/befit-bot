package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.MeasurementTypes;
import dev.jb.befit.backend.discord.commands.CommandService;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewExerciseCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseTypeService exerciseService;
    private final CommandService commandService;

    @Override
    public String getCommandNameFilter() {
        return "exercises create";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var createSubCommandOptional = event.getOption("create");
        if (createSubCommandOptional.isEmpty()) return Mono.empty();
        var createSubCommand = createSubCommandOptional.get();

        var exerciseName = createSubCommand.getOption("name").orElseThrow().getValue().orElseThrow().asString();
        var measurementTypeString = createSubCommand.getOption("measurement-type").orElseThrow().getValue().orElseThrow().asString();
        var measurementType = MeasurementTypes.valueOf(measurementTypeString);
        var goalDirectionString = createSubCommand.getOption("goal-direction").orElseThrow().getValue().orElseThrow().asString();
        var goalDirection = GoalDirection.valueOf(goalDirectionString);

        var exercise = exerciseService.create(exerciseName, measurementType, goalDirection);
        var embed = EmbedCreateSpec.builder()
                .title("Your new exercise")
                .addField(
                        String.format("#%d %s", exercise.getId(), exercise.getName()),
                        String.format("Measurement: %s\nGoal direction: %s",
                                exercise.getMeasurementType().getLongName(),
                                exercise.getGoalDirection().name().toLowerCase()
                        ),
                        false)
                .color(Color.GREEN)
                .build();

        try {
            commandService.updateCommandsWithExerciseNameOptions();
        } catch (IOException e) {
            log.error("Failed to update commands with exercise name", e);
        }

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
