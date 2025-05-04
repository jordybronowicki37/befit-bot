package dev.jb.befit.backend.discord.commands.handlers.goals;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.ServiceConstants;
import dev.jb.befit.backend.service.UserExperienceService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.visuals.UserExperienceImageService;
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
import java.io.FileNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalAddCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandGoalsAdd;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);

        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var exerciseName = CommandHandlerHelper.getOptionValue(subCommand, CommandConstants.AutoCompletePropExerciseName);
        var exerciseAmount = CommandHandlerHelper.getOptionValueAsDouble(subCommand, "amount");

        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.create(user, exerciseName, exerciseAmount);

        var embed = EmbedCreateSpec.builder()
                .title(":dart: New goal set")
                .addField(GoalsViewCommandHandler.getGoalField(goal))
                .color(Color.GREEN);

        // Add user xp field
        FileInputStream inputStream;
        {
            var userXp = user.getXp();
            var xpLevelData = UserExperienceService.getLevelData(userXp);
            var levelDescription = String.format(":dizzy: Earned: %dxp - %dxp required for next level", ServiceConstants.EarnedXpGoalCreated, xpLevelData.xpTopLevel());
            embed.addField("Experience", levelDescription, false);
            var userLevelXpBar = UserExperienceImageService.getXpLevelPicture(userXp);
            try {
                inputStream = new FileInputStream(userLevelXpBar);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            embed.image("attachment://level-xp-bar.png");
        }

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed.build()).addFile("level-xp-bar.png", inputStream).build()).then();
    }
}
