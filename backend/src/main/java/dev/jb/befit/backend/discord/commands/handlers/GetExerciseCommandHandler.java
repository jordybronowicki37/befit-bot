package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.*;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetExerciseCommandHandler extends DiscordChatInputInteractionEventListener {
    private final static Integer LEADER_BOARD_MAX_SIZE = 3;

    private final ExerciseTypeService exerciseService;
    private final ExerciseLogService exerciseLogService;
    private final GoalService goalService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return "exercises view one";
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);

        var subCommandGroup = CommandHandlerHelper.getOption(event, "view");
        var subCommand = CommandHandlerHelper.getOption(subCommandGroup, "one");
        var exerciseName = CommandHandlerHelper.getOptionValue(subCommand, "exercise-name");

        var exercise = exerciseService.getByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));
        var measurement = exercise.getMeasurementType();
        var logs = exerciseLogService.getAllByUserAndExerciseName(user, exerciseName);
        var records = ServiceHelper.sortLeaderboard(exercise.getExerciseRecords());

        var embed = EmbedCreateSpec.builder()
                .title(String.format("Exercise: #%d %s", exercise.getId(), exercise.getName()))
                .color(Color.GREEN);

        var propertiesBuilder = new StringBuilder();
        propertiesBuilder.append(String.format("\nMeasurement: %s", measurement.getLongName()));
        propertiesBuilder.append(String.format("\nGoal type: %s", exercise.getGoalDirection().name().toLowerCase()));
        propertiesBuilder.append(String.format("\nParticipants: %d", records.size()));
        embed.addField("Properties", propertiesBuilder.toString(), false);

        if (!logs.isEmpty()) {
            var progressDescriptionBuilder = new StringBuilder();
            progressDescriptionBuilder.append(String.format("Logs: %d", logs.size()));

            var lastLog = logs.get(logs.size() - 1);
            progressDescriptionBuilder.append(String.format("\nLast: %s %s - %s", CommandHandlerHelper.formatDouble(lastLog.getAmount()), measurement.getShortName(), lastLog.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE)));

            var goal = goalService.getActiveUserGoal(user, exerciseName);
            goal.ifPresent(value -> progressDescriptionBuilder.append(String.format("\nGoal: %s", CommandHandlerHelper.formatDouble(value.getAmount()))));

            progressDescriptionBuilder.append(String.format("\nPr: %s %s", CommandHandlerHelper.formatDouble(ServiceHelper.getCurrentPr(logs)), measurement.getShortName()));

            var leaderBoardPosition = ServiceHelper.getLeaderboardPosition(user, records);
            if (leaderBoardPosition != null) progressDescriptionBuilder.append(String.format("\nPosition: %s", CommandHandlerHelper.getLeaderboardValue(leaderBoardPosition)));

            embed.addField("Your progress", progressDescriptionBuilder.toString(), false);
        }

        if (!records.isEmpty()) {
            var recordsDescriptionBuilder = new StringBuilder();
            for (int i = 0; i < records.size(); i++) {
                if (i == LEADER_BOARD_MAX_SIZE) break;
                var record = records.get(i);
                recordsDescriptionBuilder.append(String.format("%s %s %s - %s\n",
                        CommandHandlerHelper.getLeaderboardValue(i+1),
                        CommandHandlerHelper.formatDouble(record.getAmount()),
                        measurement.getShortName(),
                        CommandHandlerHelper.getUserStringValue(record.getUser()))
                );
            }
            embed.addField("Leaderboard", recordsDescriptionBuilder.toString(), false);
        }

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed.build()).build()).then();
    }
}
