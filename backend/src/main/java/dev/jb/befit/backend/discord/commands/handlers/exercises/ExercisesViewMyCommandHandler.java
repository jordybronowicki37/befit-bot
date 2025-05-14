package dev.jb.befit.backend.discord.commands.handlers.exercises;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.*;
import dev.jb.befit.backend.service.exceptions.RecordNotFoundException;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExercisesViewMyCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService exerciseLogService;
    private final ExerciseRecordService exerciseRecordService;
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandExercisesViewMy;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        return event.editReply(getReplyEditSpec(userId, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var user = userService.getOrCreateDiscordUser(userId);
        var goals = goalService.getAllUserGoals(user, GoalStatus.ACTIVE);
        var allLogs = exerciseLogService.getAllByUser(user);

        var groupedLogs = allLogs.stream()
                .collect(Collectors.groupingBy(ExerciseLog::getExerciseType))
                .entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getKey().getId())).toList();

        var groupedLogsPage = CommandHandlerHelper.getPageForList(page, CommandConstants.PageSize, groupedLogs);
        var embed = EmbedCreateSpec.builder()
                .title("Your exercises")
                .fields(groupedLogsPage
                        .stream()
                        .map(groupedLog -> {
                            var exercise = groupedLog.getKey();
                            var goal = goals.stream().filter(g -> g.getExerciseType().getId().equals(exercise.getId())).findFirst();
                            var logs = groupedLog.getValue();
                            var last = exerciseLogService.getLastByUserAndExercise(user, exercise.getId());
                            var pr = exerciseRecordService.getByExercise(user, exercise).orElseThrow(RecordNotFoundException::new);
                            var measurementName = exercise.getMeasurementType().getShortName();
                            var descriptionBuilder = new StringBuilder();
                            descriptionBuilder.append("Logs: ").append(logs.size());
                            goal.ifPresent(g -> descriptionBuilder.append(String.format("\nGoal: %s %s", CommandHandlerHelper.formatDouble(g.getAmount()), measurementName)));
                            last.ifPresent(l -> descriptionBuilder.append(String.format(
                                    "\nLast: %s %s - %s",
                                    CommandHandlerHelper.formatDouble(l.getAmount()),
                                    measurementName,
                                    CommandHandlerHelper.discordTimeAgoText(l.getCreated())
                            )));
                            descriptionBuilder.append(String.format(
                                    "\nPr: %s %s - %s",
                                    CommandHandlerHelper.formatDouble(pr.getAmount()),
                                    measurementName,
                                    CommandHandlerHelper.discordTimeAgoText(pr.getExerciseLog().getCreated())
                            ));
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
