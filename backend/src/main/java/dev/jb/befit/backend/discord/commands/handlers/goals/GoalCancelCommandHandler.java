package dev.jb.befit.backend.discord.commands.handlers.goals;

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
public class GoalCancelCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final GoalService goalService;
    private final GoalsViewCommandHandler goalsViewCommandHandler;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandGoalsCancel;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);

        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var goalId = CommandHandlerHelper.getOptionValueAsLong(subCommand, CommandConstants.AutoCompletePropGoal);

        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.cancel(user, goalId);

        var embed = EmbedCreateSpec.builder()
                .title(":no_entry_sign: Goal cancelled")
                .description(String.format("Why would you cancel a goal you are working so hard for? :thinking:\nIf you would like to change the target value of a goal you can just replace it by using the %s command again.", CommandHandlerHelper.getCommandReference(CommandConstants.CommandGoalsAdd)))
                .addField(goalsViewCommandHandler.getGoalField(goal))
                .color(Color.GREEN)
                .build();

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
