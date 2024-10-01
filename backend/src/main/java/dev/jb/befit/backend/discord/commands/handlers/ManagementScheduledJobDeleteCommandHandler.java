package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ScheduledJobService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagementScheduledJobDeleteCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ScheduledJobService scheduledJobService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandManagementJobsRemove;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getOption(event, getCommandNameFilter());
        var jobId = CommandHandlerHelper.getOptionValueAsLong(subCommand, CommandConstants.AutoCompletePropScheduledJob);

        scheduledJobService.delete(jobId);

        return event.editReply(InteractionReplyEditSpec.builder().contentOrNull("Successfully removed job").build()).then();
    }
}
