package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.ServiceHelper;
import dev.jb.befit.backend.service.UserService;
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
public class MyExercisesCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService exerciseLogService;
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return "exercises view my";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var goals = goalService.getAllActiveUserGoals(user);
        var allLogs = exerciseLogService.getAllByUser(user);
        var groupedLogs = allLogs.stream().collect(Collectors.groupingBy(ExerciseLog::getExerciseType));
        var sortedGroupedLogs = groupedLogs.entrySet().stream().sorted(Comparator.comparingLong(e -> e.getKey().getId())).toList();

        var embed = EmbedCreateSpec.builder()
                .title("Your exercises")
                .fields(sortedGroupedLogs
                    .stream()
                    .map(groupedLog -> {
                        var exercise = groupedLog.getKey();
                        var goal = goals.stream().filter(g -> g.getExerciseType().getId().equals(exercise.getId())).findFirst();
                        var logs = groupedLog.getValue();
                        var pr = ServiceHelper.getCurrentPr(logs);
                        var descriptionBuilder = new StringBuilder();
                        descriptionBuilder.append("Logs: ").append(logs.size());
                        goal.ifPresent(g -> descriptionBuilder.append(String.format("\nGoal: %d %s", g.getAmount(), exercise.getMeasurementType().getShortName())));
                        descriptionBuilder.append(String.format("\nPr: %d %s", pr, exercise.getMeasurementType().getShortName()));
                        descriptionBuilder.append(String.format("\nPosition: %s", CommandHandlerHelper.getLeaderboardValue(ServiceHelper.getLeaderboardPosition(user, exercise.getExerciseRecords()))));

                        return EmbedCreateFields.Field.of(
                            String.format("#%d %s", exercise.getId(), exercise.getName()),
                            descriptionBuilder.toString(),
                            false);
                    })
                    .toList())
                .color(Color.GREEN)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
