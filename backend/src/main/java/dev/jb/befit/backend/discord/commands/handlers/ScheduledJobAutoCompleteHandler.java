package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.jobs.MessageSchedulerJobController;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputAutoCompleteEventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobAutoCompleteHandler extends DiscordChatInputAutoCompleteEventListener {
    private final MessageSchedulerJobController messageSchedulerJobController;
    private final GatewayDiscordClient discordClient;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.AutoCompletePropScheduledJob;
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        var filter = CommandHandlerHelper.getAutocompleteOptionFilter(event);

        var jobs = messageSchedulerJobController.getScheduledTasks();
        var suggestions = jobs.keySet().stream()
                .map(scheduledJob -> {
                    var jobId = scheduledJob.getId();
                    var channel = discordClient.getChannelById(scheduledJob.getChannelId()).block();
                    var channelName = channel != null ? channel.getMention() : "Unknown channel";
                    channelName += " | " + scheduledJob.getType().getDisplayName();
                    channelName += " | " + scheduledJob.getCronExpression();
                    channelName += " | " + scheduledJob.getTimeZone();
                    return ApplicationCommandOptionChoiceData.builder().name(channelName).value(jobId).build();
                })
                .filter(v -> v.name().contains(filter))
                .limit(CommandConstants.SearchResultsSize)
                .toList();
        return event.respondWithSuggestions(new ArrayList<>(suggestions));
    }
}
