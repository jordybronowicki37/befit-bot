package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.ServiceHelper;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyExercisesCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService exerciseLogService;
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandExercisesViewMy;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(userId, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var user = userService.getOrCreateDiscordUser(userId);
        var goals = goalService.getAllActiveUserGoals(user);
        var allLogs = exerciseLogService.getAllByUser(user);

        var groupedLogs = allLogs.stream()
                .collect(Collectors.groupingBy(ExerciseLog::getExerciseType))
                .entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getKey().getId())).toList();

        // Create page
        var pageRequest = PageRequest.of(page, CommandConstants.PageSize);
        var start = (int) pageRequest.getOffset();
        var end = Math.min((start + pageRequest.getPageSize()), groupedLogs.size());
        var pageContent = groupedLogs.subList(start, end);
        var groupedLogsPage = new PageImpl<>(pageContent, pageRequest, groupedLogs.size());

        var embed = EmbedCreateSpec.builder()
                .title("Your exercises")
                .fields(groupedLogsPage
                        .stream()
                        .map(groupedLog -> {
                            var exercise = groupedLog.getKey();
                            var goal = goals.stream().filter(g -> g.getExerciseType().getId().equals(exercise.getId())).findFirst();
                            var logs = groupedLog.getValue();
                            var pr = ServiceHelper.getCurrentPr(logs);
                            var descriptionBuilder = new StringBuilder();
                            descriptionBuilder.append("Logs: ").append(logs.size());
                            goal.ifPresent(g -> descriptionBuilder.append(String.format("\nGoal: %s %s", CommandHandlerHelper.formatDouble(g.getAmount()), exercise.getMeasurementType().getShortName())));
                            descriptionBuilder.append(String.format("\nPr: %s %s", CommandHandlerHelper.formatDouble(pr), exercise.getMeasurementType().getShortName()));
                            var leaderBoardPos = ServiceHelper.getLeaderboardPosition(user, exercise.getExerciseRecords());
                            if (leaderBoardPos != null) descriptionBuilder.append(String.format("\nPosition: %s", CommandHandlerHelper.getLeaderboardValue(leaderBoardPos)));

                            return EmbedCreateFields.Field.of(
                                    String.format("#%d %s", exercise.getId(), exercise.getName()),
                                    descriptionBuilder.toString(),
                                    false);
                        })
                        .toList())
                .color(Color.GREEN)
                .build();

        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, groupedLogsPage.getTotalPages(), getCommandNameFilter());
        return InteractionReplyEditSpec.builder().addEmbed(embed).addComponent(paginationControls).build();
    }
}
