package dev.jb.befit.backend.discord.jobs;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduledJobGymReminderService {
    private final GatewayDiscordClient client;

    public void publishGymReminderQuote(Snowflake gymReminderChannelId) {
        log.info("Publishing gym reminder");
        client
                .getChannelById(gymReminderChannelId)
                .ofType(MessageChannel.class)
                .flatMap(c -> {
                    var builder = EmbedCreateSpec.builder()
                            .title(":clock7: Daily reminder")
                            .description("Have you done your daily exercise?")
                            .footer("Now is the time to get your lazy ass off the couch", null)
                            .color(Color.CYAN);
                    return c.createMessage(builder.build());
                })
                .block();
    }
}
