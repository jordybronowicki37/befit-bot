package dev.jb.befit.backend.discord.commands.handlers.help;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields.Field;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpCommandHandler extends DiscordChatInputInteractionEventListener {
    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHelp;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var avatarUrl = event.getClient().getSelf().block().getAvatarUrl();

        var embed = EmbedCreateSpec.builder()
                .author("Befit bot", "https://github.com/jordybronowicki37/befit-bot", avatarUrl)
                .title("About this bot")
                .description("This bot helps you track your gym progress and motivates you into a better lifestyle.\n## Useful commands:")
                .addFields(
                        Field.of("/log", "Create a new log for an exercise. The more logs you create the better the bot can help you improve.", false),
                        Field.of("/exercises create", "Don't see your favourite exercise inside of our catalogue? Just add it and track your progress for it!", false),
                        Field.of("/exercises view ...", "View all the exercises, your own exercises or detailed information about a single exercise.", false),
                        Field.of("/goals add", "Set goals for you to reach and work towards them.", false),
                        Field.of("/progress", "Get a progress chart on a single exercise.", false),
                        Field.of("/achievements", "See which achievements you have completed and which are still locked.", false),
                        Field.of("/motivation", "If you are looking for motivation, just use this command and get some.", false)
                )
                .color(Color.GRAY)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
