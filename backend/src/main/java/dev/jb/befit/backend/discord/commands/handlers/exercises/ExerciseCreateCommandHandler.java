package dev.jb.befit.backend.discord.commands.handlers.exercises;

import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.MeasurementType;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseCreateCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseTypeService exerciseService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandExercisesCreate;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());

        var exerciseName = CommandHandlerHelper.getOptionValue(subCommand, "name");
        var measurementType = MeasurementType.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "measurement-type"));
        var goalDirection = GoalDirection.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "goal-direction"));

        var exercise = exerciseService.create(exerciseName, measurementType, goalDirection);
        var embed = EmbedCreateSpec.builder()
                .title("Your new exercise")
                .addField(
                        String.format("#%d %s", exercise.getId(), exercise.getName()),
                        String.format("Measurement: %s\nGoal direction: %s\n\n_Use %s to add a log for this exercise._\n_Use %s to add a goal for this exercise._",
                                exercise.getMeasurementType().getLongName(),
                                exercise.getGoalDirection().name().toLowerCase(),
                                CommandHandlerHelper.getCommandReference(CommandConstants.CommandLog),
                                CommandHandlerHelper.getCommandReference(CommandConstants.CommandGoalsAdd)
                        ),
                        false)
                .color(Color.GREEN)
                .build();

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
