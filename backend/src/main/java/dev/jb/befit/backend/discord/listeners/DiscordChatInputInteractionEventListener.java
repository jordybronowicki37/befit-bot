package dev.jb.befit.backend.discord.listeners;

import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.service.exceptions.MyException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

import static dev.jb.befit.backend.discord.commands.CommandHandlerHelper.getOptions;

@Slf4j
public abstract class DiscordChatInputInteractionEventListener implements DiscordEventListener<ChatInputInteractionEvent> {
    public abstract String getCommandNameFilter();

    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    public boolean acceptExecution(ChatInputInteractionEvent event) {
        return CommandHandlerHelper.checkCommandName(event, getCommandNameFilter());
    }

    public void logExecution(ChatInputInteractionEvent event) {
        var options = getOptions(event, getCommandNameFilter());
        var optionValues = options.stream()
                .map(ApplicationCommandInteractionOption::getValue)
                .map(v -> v.map(ApplicationCommandInteractionOptionValue::getRaw).orElse(""))
                .toArray(String[]::new);
        log.info(
                "Executing command handler: {}, discord user id: {}, username: {}, options: [{}]",
                getCommandNameFilter(),
                CommandHandlerHelper.getDiscordUserId(event).asString(),
                CommandHandlerHelper.getDiscordUserName(event),
                String.join(", ", optionValues)
        );
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
            log.error("An handled error has occurred", error);
            return editReplyToError(initialEvent, "Something went wrong", error.getMessage());
        }
        log.error("An unhandled error has occurred", error);
        return editReplyToError(initialEvent, "Something went wrong", "Please try again later.");
    }

    public static Mono<Void> editReplyToError(ChatInputInteractionEvent event, String title, String description) {
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
