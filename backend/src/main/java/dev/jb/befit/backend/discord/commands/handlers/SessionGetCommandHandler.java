package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseSessionService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.exceptions.SessionNotFoundException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionGetCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsViewOne;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var sessionId = CommandHandlerHelper.getOptionValueAsLong(subCommand, CommandConstants.AutoCompletePropSession);

        var user = userService.getOrCreateDiscordUser(userId);
        var session = exerciseSessionService.getByUserAndId(user, sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        var embed = getEmbed(session).build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }

    public static EmbedCreateSpec.Builder getEmbed(ExerciseSession session) {
        var logs = session.getExerciseLogs();
        var logsDescriptionBuilder = new StringBuilder();

        if (logs.isEmpty()) logsDescriptionBuilder.append("_You have not added any logs yet._");
        else {
            logs.stream().sorted(Comparator.comparing(ExerciseLog::getCreated)).forEach(log -> {
                var exercise = log.getExerciseType();
                var measurement = exercise.getMeasurementType();
                var amount = CommandHandlerHelper.formatDouble(log.getAmount());
                var time = CommandHandlerHelper.formatTime(log.getCreated());
                logsDescriptionBuilder.append(String.format("**#%d %s**\nAmount: %s %s\nCreated: %s\n\n", exercise.getId(), exercise.getName(), amount, measurement.getShortName(), time));
            });
        }

        var descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(String.format("Name: %s\n", session.getName()));
        descriptionBuilder.append(String.format("Started: %s\n", CommandHandlerHelper.formatDateTime(session.getCreated())));
        descriptionBuilder.append(String.format("Status: %s\n", session.getStatus().name().toLowerCase()));
        descriptionBuilder.append(String.format("Total logs: %d\n", logs.size()));
        descriptionBuilder.append(String.format("### Logs\n%s", logsDescriptionBuilder));

        return EmbedCreateSpec.builder()
                .title("Session")
                .description(descriptionBuilder.toString())
                .color(Color.GREEN);
    }
}
