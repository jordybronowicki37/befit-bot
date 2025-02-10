package dev.jb.befit.backend.discord.commands.handlers.stats;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.*;
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
public class StatsCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final ExerciseLogService exerciseLogService;
    private final ExerciseSessionService exerciseSessionService;
    private final GoalService goalService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandStats;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userOption = event.getOption("user");
        var discordUser = event.getInteraction().getUser();
        if (userOption.isPresent()) {
            var userOptionValue = userOption.get().getValue();
            if (userOptionValue.isPresent()) {
                discordUser = userOptionValue.get().asUser().block();
            }
        }

        var user = userService.getOrCreateDiscordUser(discordUser.getId());

        var description = new StringBuilder();
        description.append(String.format("Account created: %s\n", CommandHandlerHelper.discordFormatDate(user.getCreated())));
        description.append(String.format("Achievements completed: %d/%d\n", user.getAchievements().size(), Achievement.values().length));
        description.append(String.format("Goals active: %d\n", goalService.getAllUserGoals(user, GoalStatus.ACTIVE).size()));
        description.append(String.format("Goals completed: %d\n", goalService.getAllUserGoals(user, GoalStatus.COMPLETED).size()));
        description.append(String.format("Last log: %s\n", exerciseLogService.getLastByUser(user).map(l -> CommandHandlerHelper.discordFormatDateTime(l.getCreated())).orElse("`Not available`")));
        description.append(String.format("Logs total: %d\n", exerciseLogService.countAllByUser(user)));
        description.append(String.format("Last session: %s\n", exerciseSessionService.getLastByUser(user).map(s -> CommandHandlerHelper.discordFormatDateTime(s.getCreated())).orElse("`Not available`")));
        description.append(String.format("Sessions total: %d\n", exerciseSessionService.amountByUser(user)));
        description.append(String.format("Participated exercises: %d\n", exerciseLogService.countAmountOfExercisesByUser(user)));

        var embed = EmbedCreateSpec.builder()
                .author(discordUser.getUsername(), null, discordUser.getAvatarUrl())
                .title("Stats")
                .description(description.toString())
                .color(Color.CYAN);

        // Add user xp field
        FileInputStream inputStream;
        {
            var xpLevelData = UserExperienceService.getLevelData(user.getXp());
            var levelDescription = String.format("\n:dizzy: %dxp required for next level", xpLevelData.xpTopLevel());
            embed.addField("Experience", levelDescription, false);
            var userLevelXpBar = UserExperienceImageService.getXpLevelPicture(user.getXp());
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
