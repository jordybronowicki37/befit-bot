package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.UserExperienceService;
import dev.jb.befit.backend.service.UserService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandLeaderboard;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var user = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(user, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var pageSize = CommandConstants.PageSizeSmallItems;
        var usersPage = userService.getUsersOrderedByXp(Pageable.ofSize(pageSize).withPage(page));
        var userRank = userService.getUserLeaderboardRank(userService.getOrCreateDiscordUser(userId).getId());
        var users = usersPage.getContent();
        var description = new StringBuilder();
        description.append(String.format("Your rank: #%d\n", userRank));

        for (int i = 0; i < users.size(); i++) {
            var index = (page * pageSize) + i + 1;
            var user = users.get(i);
            var userName = CommandHandlerHelper.getUserStringValue(user);
            var levelData = UserExperienceService.getLevelData(user.getXp());
            description.append(String.format("\n**%s %s**\n", CommandHandlerHelper.getLeaderboardValue(index), userName));
            description.append(String.format("Level %d\n", levelData.level()));
        }

        var embed = EmbedCreateSpec.builder()
                .title("Leaderboard")
                .description(description.toString())
                .color(Color.CYAN);
        var controls = CommandHandlerHelper.getPaginationComponent(page, usersPage.getTotalPages(), getCommandNameFilter());
        return InteractionReplyEditSpec.builder().addEmbed(embed.build()).addComponent(controls).build();
    }
}
