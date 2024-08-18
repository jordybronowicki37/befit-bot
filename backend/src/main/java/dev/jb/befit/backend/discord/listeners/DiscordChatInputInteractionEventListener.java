package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.exceptions.MyException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class DiscordChatInputInteractionEventListener implements DiscordEventListener<ChatInputInteractionEvent> {
    public abstract String getCommandNameFilter();

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
            var embed = EmbedCreateSpec.builder()
                    .title("Something went wrong")
                    .description(error.getMessage())
                    .color(Color.RED)
                    .build();
            return initialEvent.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
        }
        else {
            log.error("An unhandled error has occurred", error);
            var embed = EmbedCreateSpec.builder()
                    .title("Something went wrong")
                    .description("Please try again later.")
                    .color(Color.RED)
                    .build();
            return initialEvent.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
        }
    }
}
