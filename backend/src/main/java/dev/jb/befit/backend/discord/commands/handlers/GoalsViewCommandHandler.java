package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.UserService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalsViewCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandGoalsView;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(userId, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var pageSize = CommandConstants.PageSize;
        var user = userService.getOrCreateDiscordUser(userId);
        var goals = goalService.getAllUserGoals(user, GoalStatus.ACTIVE);

        var embed = EmbedCreateSpec.builder()
                .title(":chart_with_upwards_trend: Your goals")
                .color(Color.GREEN);

        if (goals.isEmpty()) embed.description("No goals yet, try creating a goal by using the command: /" + CommandConstants.CommandGoalsAdd);

        goals.stream().skip((long) pageSize * page).limit(pageSize)
                .sorted(Comparator.comparing(g -> g.getExerciseType().getName()))
                .forEach(g -> {
                    var description = String.format("Created: %s\nAmount: %s %s", CommandHandlerHelper.timeAgoText(g.getCreated().toLocalDate()), CommandHandlerHelper.formatDouble(g.getAmount()), g.getExerciseType().getMeasurementType().getShortName());
                    embed.addField(g.getExerciseType().getName(), description, false);
                });

        var amountOfPages = CommandHandlerHelper.getAmountOfPages(goals.size(), pageSize);
        var controls = CommandHandlerHelper.getPaginationComponent(page, amountOfPages, getCommandNameFilter());

        return InteractionReplyEditSpec.builder().addEmbed(embed.build()).addComponent(controls).build();
    }
}
