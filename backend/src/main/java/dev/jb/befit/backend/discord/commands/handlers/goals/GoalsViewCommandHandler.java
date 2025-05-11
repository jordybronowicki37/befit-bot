package dev.jb.befit.backend.discord.commands.handlers.goals;

import dev.jb.befit.backend.data.models.Goal;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.ExerciseRecordService;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.UserService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
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
    private final ExerciseLogService exerciseLogService;
    private final ExerciseRecordService exerciseRecordService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandGoalsView;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        return event.editReply(getReplyEditSpec(userId, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var pageSize = CommandConstants.PageSize;
        var user = userService.getOrCreateDiscordUser(userId);
        var goals = goalService.getAllUserGoals(user, GoalStatus.ACTIVE);

        var embed = EmbedCreateSpec.builder()
                .title(":dart: Your goals")
                .color(Color.GREEN);

        if (goals.isEmpty()) embed.description(String.format("No goals yet, try creating a goal by using the command: `/%s`", CommandConstants.CommandGoalsAdd));

        goals.stream().skip((long) pageSize * page).limit(pageSize)
                .sorted(Comparator.comparing(Goal::getCreated).reversed())
                .forEach(g -> embed.addField(getGoalField(g)));

        var amountOfPages = CommandHandlerHelper.getAmountOfPages(goals.size(), pageSize);
        var controls = CommandHandlerHelper.getPaginationComponent(page, amountOfPages, getCommandNameFilter());

        return InteractionReplyEditSpec.builder().addEmbed(embed.build()).addComponent(controls).build();
    }

    public EmbedCreateFields.Field getGoalField(Goal goal) {
        var exerciseType = goal.getExerciseType();
        var measurement = exerciseType.getMeasurementType().getShortName();
        var title = String.format("#%d %s", exerciseType.getId(), exerciseType.getName());
        var pr = exerciseRecordService.getByExercise(goal.getUser(), exerciseType.getName());
        var lastLog = exerciseLogService.getLastByUserAndExercise(goal.getUser(), exerciseType.getId());

        var description = new StringBuilder();
        description.append(String.format("Status: %s\n", goal.getStatus().name().toLowerCase()));
        description.append(String.format("Created: %s\n", CommandHandlerHelper.discordTimeAgoText(goal.getCreated())));
        description.append(String.format(
                "Amount: %s %s\n",
                CommandHandlerHelper.formatDouble(goal.getAmount()),
                measurement
        ));
        lastLog.map(l -> description.append(String.format(
                "Last log: %s %s - %s\n",
                CommandHandlerHelper.formatDouble(l.getAmount()),
                measurement,
                CommandHandlerHelper.discordTimeAgoText(l.getCreated())
        )));
        pr.map(p -> description.append(String.format(
                "Pr: %s %s\n",
                CommandHandlerHelper.formatDouble(p.getAmount()),
                measurement
        )));

        return EmbedCreateFields.Field.of(title, description.toString(), false);
    }
}
