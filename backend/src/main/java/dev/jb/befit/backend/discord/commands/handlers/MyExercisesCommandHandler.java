package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyExercisesCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseLogService exerciseLogService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return "exercises view my";
    }

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var allLogs = exerciseLogService.getAllByUser(user);
        var groupedLogs = allLogs.stream().collect(Collectors.groupingBy(ExerciseLog::getExerciseType));

        var sortedGroupedLogs = groupedLogs.entrySet().stream().sorted(Comparator.comparingLong(e -> e.getKey().getId())).toList();

        var embed = EmbedCreateSpec.builder()
                .title("Your exercises")
                .fields(sortedGroupedLogs
                    .stream()
                    .map(groupedLog -> {
                        var exercise = groupedLog.getKey();
                        var logs = groupedLog.getValue();
                        var pr = getCurrentPr(logs);
                        return EmbedCreateFields.Field.of(
                            String.format("#%d %s", exercise.getId(), exercise.getName()),
                            String.format("Logs: %d\nPr: %d", logs.size(), pr),
                            false);
                    })
                    .toList())
                .color(Color.GREEN)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }

    private Integer getCurrentPr(List<ExerciseLog> allExerciseLogs) {
        if (allExerciseLogs.isEmpty()) return null;
        var exerciseType = allExerciseLogs.get(0).getExerciseType();
        var exerciseAmounts = allExerciseLogs.stream().map(ExerciseLog::getAmount).toList();
        if (exerciseType.getGoalDirection().equals(GoalDirection.INCREASING)) {
            return exerciseAmounts.stream().max(Integer::compareTo).orElse(null);
        } else {
            return exerciseAmounts.stream().min(Integer::compareTo).orElse(null);
        }
    }
}
