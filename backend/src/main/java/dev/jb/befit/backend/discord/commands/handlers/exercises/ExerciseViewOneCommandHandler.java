package dev.jb.befit.backend.discord.commands.handlers.exercises;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.*;
import dev.jb.befit.backend.service.visuals.ProgressImageService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseViewOneCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseTypeService exerciseService;
    private final ExerciseLogService exerciseLogService;
    private final ExerciseRecordService exerciseRecordService;
    private final ProgressImageService progressImageService;
    private final GoalService goalService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandExercisesViewOne;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        var user = userService.getOrCreateDiscordUser(userId);

        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var exerciseName = CommandHandlerHelper.getOptionValue(subCommand, CommandConstants.AutoCompletePropExerciseName);

        var exercise = exerciseService.findByName(exerciseName);
        var measurement = exercise.getMeasurementType();
        var logs = exerciseLogService.getAllByUserAndExerciseName(user, exerciseName);
        var myRecord = exerciseRecordService.getByExercise(user, exercise);
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
            progressDescriptionBuilder.append(String.format(
                    "\nLast: %s %s - %s",
                    CommandHandlerHelper.formatDouble(lastLog.getAmount()),
                    measurement.getShortName(),
                    CommandHandlerHelper.discordFormatDate(lastLog.getCreated())
            ));

            myRecord.ifPresent(exerciseRecord ->
                    progressDescriptionBuilder.append(String.format(
                            "\nPr: %s %s - %s",
                            CommandHandlerHelper.formatDouble(exerciseRecord.getAmount()),
                            measurement.getShortName(),
                            CommandHandlerHelper.discordTimeAgoText(exerciseRecord.getExerciseLog().getCreated())
                    )));

            var goal = goalService.getActiveUserGoal(user, exerciseName);
            goal.ifPresent(value -> progressDescriptionBuilder.append(String.format("\nGoal: %s %s", CommandHandlerHelper.formatDouble(value.getAmount()), measurement.getShortName())));

            var leaderBoardPosition = ServiceHelper.getLeaderboardPosition(user, records);
            if (leaderBoardPosition != null) progressDescriptionBuilder.append(String.format("\nPosition: %s", CommandHandlerHelper.getLeaderboardValue(leaderBoardPosition)));

            embed.addField("Your progress", progressDescriptionBuilder.toString(), false);
        }

        if (!records.isEmpty()) {
            var recordsDescriptionBuilder = new StringBuilder();
            for (int i = 0; i < records.size(); i++) {
                if (i == CommandConstants.PageSizeSmallItems) break;
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

        var reply = InteractionReplyEditSpec.builder();

        try {
            var progressImage = progressImageService.createGlobalProgressChart(userId, exerciseName);
            var inputStream = new FileInputStream(progressImage);
            reply.addFile("progress.png", inputStream);
            embed.image("attachment://progress.png");
        } catch (Exception ignored) {}

        reply.addEmbed(embed.build());

        return event.editReply(reply.build()).then();
    }
}
