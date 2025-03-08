package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.exceptions.MyException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public abstract class DiscordChatInputInteractionEventListener implements DiscordEventListener<ChatInputInteractionEvent> {
    public abstract String getCommandNameFilter();

    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    public boolean acceptExecution(ChatInputInteractionEvent event) {
        return CommandHandlerHelper.checkCommandName(event, getCommandNameFilter());
    }

    public Mono<ChatInputInteractionEvent> preExecute(ChatInputInteractionEvent event) {
        return event.deferReply().then(Mono.just(event));
    }

    public Mono<Void> handleError(Throwable error) {
        log.error("Unable to process: {}", getEventType().getSimpleName(), error);
        return Mono.empty();
    }

    public Mono<Void> replyWithErrorMessage(Throwable error, ChatInputInteractionEvent initialEvent) {
        if (error instanceof MyException) {
            return editReplyToError(initialEvent, "Something went wrong", error.getMessage());
        }
        else {
            log.error("An unhandled error has occurred", error);
            return editReplyToError(initialEvent, "Something went wrong", "Please try again later.");
        }
    }

    public static Mono<Void> editReplyToError(DeferrableInteractionEvent event, String title, String description) {
        var embed = EmbedCreateSpec.builder()
                .title(title)
                .description(description)
                .color(Color.RED)
                .build();
        var reply = InteractionReplyEditSpec
                .builder()
                .embeds(List.of(embed))
                .componentsOrNull(null)
                .contentOrNull(null)
                .build();
        return event
                .editReply(reply)
                .flatMap(m -> m.edit().withAttachments())
                .then();
    }
}
