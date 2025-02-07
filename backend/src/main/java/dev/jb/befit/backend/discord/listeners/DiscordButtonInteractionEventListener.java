package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.service.exceptions.MyException;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class DiscordButtonInteractionEventListener implements DiscordEventListener<ButtonInteractionEvent> {
    public abstract String getCommandNameFilter();

    public Class<ButtonInteractionEvent> getEventType() {
        return ButtonInteractionEvent.class;
    }

    public boolean acceptExecution(ButtonInteractionEvent event) {
        return event.getCustomId().startsWith(getCommandNameFilter()+'$');
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
