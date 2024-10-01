package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.AchievementIcon;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.discord.registration.EmojiRegistrarService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllAchievementsCommandHandler extends DiscordChatInputInteractionEventListener {
    private static final long PAGE_SIZE = 5;

    private final EmojiRegistrarService emojiRegistrarService;

    @Override
    public String getCommandNameFilter() {
        return "achievements all";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.editReply(getAchievementsEditSpec(0)).then();
    }

    public InteractionReplyEditSpec getAchievementsEditSpec(long page) {
        var description = new StringBuilder();
        var allAchievements = AchievementIcon.values();
        Arrays.stream(allAchievements)
                .sorted(Comparator.comparing(AchievementIcon::getTitle))
                .skip(page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .forEach(a -> {
                    var emoji = emojiRegistrarService.getEmojiId(a, false);
                    var title = String.format("# <:%s:%s> %s\n%s\n", a.getDisplayName(), emoji.asString(), a.getTitle(), a.getDescription());
                    description.append(title);
                });
        var embed = EmbedCreateSpec.builder()
                .title("All achievements")
                .description(description.toString())
                .color(Color.CYAN)
                .build();

        var previousButton = Button.secondary(String.format("achievements all$%d", page-1), ReactionEmoji.unicode("⬅"));
        if (page <= 0) previousButton = previousButton.disabled();
        var nextButton = Button.secondary(String.format("achievements all$%d", page+1), ReactionEmoji.unicode("➡"));
        if (page == (allAchievements.length / PAGE_SIZE) - 1) nextButton = nextButton.disabled();

        return InteractionReplyEditSpec.builder().addEmbed(embed).addComponent(ActionRow.of(previousButton, nextButton)).build();
    }
}
