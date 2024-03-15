package dev.jb.befit.backend.discord.jobs;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduledGymReminder {
    private final GatewayDiscordClient client;

    @Value("${discord.channels.gym-reminder}")
    private String gymReminderChannelId;

    @Scheduled(cron = "0 0 19 * * *", zone = "Europe/Amsterdam")
    public void publishGymReminderQuote() {
        log.info("Publishing gym reminder");
        client
                .getChannelById(Snowflake.of(gymReminderChannelId))
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
