package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.UserService;
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
public class GoalAddCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandGoalsAdd;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();

        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var exerciseName = CommandHandlerHelper.getOptionValue(subCommand, CommandConstants.AutoCompletePropExerciseName);
        var exerciseAmount = CommandHandlerHelper.getOptionValueAsDouble(subCommand, "amount");

        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.create(user, exerciseName, exerciseAmount);
        var exerciseType = goal.getExerciseType();

        var title = String.format("Exercise #%d %s", exerciseType.getId(), exerciseType.getName());
        var description = String.format("Amount: %s %s\nStatus: %s", CommandHandlerHelper.formatDouble(exerciseAmount), exerciseType.getMeasurementType().getShortName(), goal.getStatus().name().toLowerCase());

        var embed = EmbedCreateSpec.builder()
                .title(":chart_with_upwards_trend: New goal set")
                .addField(title, description, false)
                .color(Color.GREEN)
                .build();

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
