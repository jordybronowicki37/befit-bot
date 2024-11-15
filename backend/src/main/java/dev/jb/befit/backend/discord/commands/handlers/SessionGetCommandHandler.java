package dev.jb.befit.backend.discord.commands.handlers;

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
        var logsDescription = new StringBuilder();

        if (logs.isEmpty()) logsDescription.append("You have not added any logs yet.");
        else {
            logs.forEach(log -> {
                var exercise = log.getExerciseType();
                var measurement = exercise.getMeasurementType();
                var amount = CommandHandlerHelper.formatDouble(log.getAmount());
                var time = CommandHandlerHelper.formatTime(log.getCreated().toLocalTime());
                logsDescription.append(String.format("### #%d %s\nAmount: %s %s\nCreated: %s", exercise.getId(), exercise.getName(), amount, measurement.getShortName(), time));
            });
        }

        return EmbedCreateSpec.builder()
                .title("Session")
                .description(String.format("Session name: %s\nStarted: %s\n", session.getName(), CommandHandlerHelper.formatDateTime(session.getCreated())))
                .addField("Logs", logsDescription.toString(), false)
                .color(Color.GREEN);
    }
}
