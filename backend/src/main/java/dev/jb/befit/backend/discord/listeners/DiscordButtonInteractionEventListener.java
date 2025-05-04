package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.exceptions.MyException;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Slf4j
public abstract class DiscordButtonInteractionEventListener implements DiscordEventListener<ButtonInteractionEvent> {
    public abstract String getCommandNameFilter();

    public Class<ButtonInteractionEvent> getEventType() {
        return ButtonInteractionEvent.class;
    }

    public boolean acceptExecution(ButtonInteractionEvent event) {
        return event.getCustomId().startsWith(getCommandNameFilter()+'$');
    }

    public void logExecution(ButtonInteractionEvent event) {
        var customIdSplit = event.getCustomId().split("\\$");
        log.info(
                "Executing button handler: {}, discord user id: {}, username: {}, type: {}, options: [{}]",
                getCommandNameFilter(),
                CommandHandlerHelper.getDiscordUserId(event).asString(),
                CommandHandlerHelper.getDiscordUserName(event),
                customIdSplit[1],
                String.join(", ", Arrays.copyOfRange(customIdSplit, 2, customIdSplit.length))
        );
    }

    public Mono<ButtonInteractionEvent> preExecute(ButtonInteractionEvent event) {
        return event.deferEdit().then(Mono.just(event));
    }

    public Mono<Void> handleError(Throwable error) {
        log.error("Unable to process: {}", getEventType().getSimpleName(), error);
        return Mono.empty();
    }

    public Mono<Void> replyWithErrorMessage(Throwable error, ButtonInteractionEvent initialEvent) {
        if (error instanceof MyException) {
            log.error("An handled error has occurred", error);
            return editReplyToError(initialEvent, "Something went wrong", error.getMessage());
        }
        log.error("An unhandled error has occurred", error);
        return editReplyToError(initialEvent, "Something went wrong", "Please try again later.");
    }

    public static Mono<Void> editReplyToError(ButtonInteractionEvent event, String title, String description) {
        var embed = EmbedCreateSpec.builder()
                .title(title)
                .description(description)
                .color(Color.RED)
                .build();

        return Mono.just(event)
                .flatMap(DeferrableInteractionEvent::getReply)
                .flatMap(Message::getChannel)
                .flatMap(channel -> channel.createMessage(embed))
                .then();
    }
}
