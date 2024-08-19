package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return "goals add";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommandOptional = event.getOption("add");
        if (subCommandOptional.isEmpty()) return Mono.empty();
        var subCommand = subCommandOptional.get();

        var exerciseName = subCommand.getOption("exercise-name").orElseThrow().getValue().orElseThrow().asString();
        var exerciseAmount = Math.toIntExact(subCommand.getOption("amount").orElseThrow().getValue().orElseThrow().asLong());
        var userId = event.getInteraction().getUser().getId();

        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.create(user, exerciseName, exerciseAmount);
        var exerciseType = goal.getExerciseType();

        var workoutTitle = String.format("Exercise #%d %s", exerciseType.getId(), exerciseType.getName());
        var description = String.format("Amount: %d\nStatus: %s", exerciseAmount, goal.getStatus().name().toLowerCase());

        var embed = EmbedCreateSpec.builder()
                .title(":chart_with_upwards_trend: New goal set")
                .addField(workoutTitle, description, false)
                .color(Color.GREEN)
                .build();

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
