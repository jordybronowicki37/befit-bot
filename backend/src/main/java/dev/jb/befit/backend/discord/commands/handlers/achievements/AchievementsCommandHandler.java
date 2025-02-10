package dev.jb.befit.backend.discord.commands.handlers.achievements;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.discord.registration.EmojiRegistrarService;
import dev.jb.befit.backend.service.UserAchievementService;
import dev.jb.befit.backend.service.UserService;
import discord4j.common.util.Snowflake;
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
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementsCommandHandler extends DiscordChatInputInteractionEventListener {
    private final EmojiRegistrarService emojiRegistrarService;
    private final UserService userService;
    private final UserAchievementService achievementService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandAchievements;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(userId, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var user = userService.getOrCreateDiscordUser(userId);
        var userAchievements = user.getAchievements();
        int pageSize = CommandConstants.PageSize;
        var description = new StringBuilder();
        var allAchievements = Achievement.values();
        Arrays.stream(allAchievements)
                .sorted((a, b) -> {
                    var difficultyCompare = a.getDifficulty().compareTo(b.getDifficulty());
                    if (difficultyCompare != 0) return difficultyCompare;
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                })
                .skip((long) page * pageSize)
                .limit(pageSize)
                .forEach(a -> {
                    var userAchievement = userAchievements.stream().filter(u -> u.getAchievement().equals(a)).findFirst();
                    var emoji = emojiRegistrarService.getEmojiId(a, userAchievement.isEmpty());

                    description.append(String.format("# <:%s:%s> %s\n***%s***\n", a.getDisplayName(), emoji.asString(), a.getTitle(), a.getDescription()));

                    if (userAchievement.isPresent()) {
                        var date = userAchievement.get().getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        description.append(String.format("Completed at: %s\n", date));
                    } else {
                        description.append("Locked.\n");
                    }

                    var completedPercentage = achievementService.getCompletionPercentage(a);
                    if (completedPercentage != 0) description.append(String.format("Community: %.1f%%\n", completedPercentage));
                });
        var embed = EmbedCreateSpec.builder()
                .title("All achievements")
                .description(description.toString())
                .color(Color.CYAN)
                .build();

        var amountOfPages = CommandHandlerHelper.getAmountOfPages(allAchievements.length, pageSize);
        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, amountOfPages, getCommandNameFilter());
        return InteractionReplyEditSpec.builder().addEmbed(embed).addComponent(paginationControls).build();
    }
}
