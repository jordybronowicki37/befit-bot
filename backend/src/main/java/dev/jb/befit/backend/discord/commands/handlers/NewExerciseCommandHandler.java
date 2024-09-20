package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.MeasurementTypes;
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
public class NewExerciseCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseTypeService exerciseService;

    @Override
    public String getCommandNameFilter() {
        return "exercises create";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getOption(event, "create");

        var exerciseName = CommandHandlerHelper.getOptionValue(subCommand, "name");
        var measurementType = MeasurementTypes.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "measurement-type"));
        var goalDirection = GoalDirection.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "goal-direction"));

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

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
