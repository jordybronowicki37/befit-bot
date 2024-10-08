package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalRemoveCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandGoalsRemove;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();

        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var goalId = CommandHandlerHelper.getOptionValueAsLong(subCommand, CommandConstants.AutoCompletePropGoal);

        var user = userService.getOrCreateDiscordUser(userId);
        goalService.cancel(user, goalId);

        var embed = EmbedCreateSpec.builder()
                .title("Goal removed")
                .description("Goal successfully removed")
                .color(Color.GREEN)
                .build();

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
