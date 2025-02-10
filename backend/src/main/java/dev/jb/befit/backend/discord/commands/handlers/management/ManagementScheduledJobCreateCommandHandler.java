package dev.jb.befit.backend.discord.commands.handlers.management;

import dev.jb.befit.backend.data.models.ScheduledJob;
import dev.jb.befit.backend.data.models.ScheduledJobType;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.commands.exceptions.InvalidValueException;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ScheduledJobService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.DateTimeException;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagementScheduledJobCreateCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ScheduledJobService scheduledJobService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandManagementJobsAdd;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var channelId = CommandHandlerHelper.getOptionValue(subCommand, "channel-id");
        var jobType = ScheduledJobType.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "job-type"));
        var cron = CommandHandlerHelper.getOptionValue(subCommand, "cron");
        var timezone = CommandHandlerHelper.getOptionalOptionValue(subCommand, "timezone-id", "UTC");

        ZoneId timezoneId;
        try {
            timezoneId = ZoneId.of(timezone);
        } catch (DateTimeException e) {
            throw new InvalidValueException("timezone-id", timezone);
        }

        try {
            CronExpression.parse(cron);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("cron", cron);
        }

        var job = new ScheduledJob(jobType, Snowflake.of(channelId), cron, timezoneId);
        scheduledJobService.create(job);

        return event.editReply(InteractionReplyEditSpec.builder().contentOrNull("Successfully created job").build()).then();
    }
}
