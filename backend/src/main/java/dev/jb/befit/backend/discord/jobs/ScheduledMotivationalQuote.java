package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.service.MotivationalService;
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
public class ScheduledMotivationalQuote {
    private final GatewayDiscordClient client;
    private final MotivationalService motivationalService;

    @Value("${discord.channels.motivational}")
    private String motivationChannelId;

    @Scheduled(cron = "0 0 8 * * *", zone = "Europe/Amsterdam")
    public void publishMotivationalQuote() {
        var message = motivationalService.getRandomQuote();
        log.info("Publishing quote. Message: {}, author: {}", message.message(), message.author());
        client
                .getChannelById(Snowflake.of(motivationChannelId))
                .ofType(MessageChannel.class)
                .flatMap(c -> {
                    var quote = motivationalService.getRandomQuote();
                    var builder = EmbedCreateSpec.builder()
                            .title(":rocket: Quote of the day")
                            .description(quote.message())
                            .footer(String.format("- %s", quote.author()), null)
                            .color(Color.GREEN);
                    return c.createMessage(builder.build());
                })
                .block();
    }
}
