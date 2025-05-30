package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.UserAchievement;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.discord.registration.EmojiRegistrarService;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionViewOneCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;
    private static final String itemSpacing = "\u200B \u200B \u200B ";

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsViewOne;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var sessionId = CommandHandlerHelper.getOptionValueAsLong(subCommand, CommandConstants.AutoCompletePropSession);
        var user = userService.getOrCreateDiscordUser(userId);
        var session = exerciseSessionService.getByUserAndId(user, sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        return event.editReply(getReplyEditSpec(session, SessionCommandType.VIEW, 0)).then();
    }

    public static InteractionReplyEditSpec getReplyEditSpec(ExerciseSession session, SessionCommandType commandType, int page) {
        var logs = session.getExerciseLogs();
        var logsPage = CommandHandlerHelper.getPageForList(page, CommandConstants.PageSize, logs);
        var logsDescriptionBuilder = new StringBuilder();

        if (logsPage.isEmpty()) logsDescriptionBuilder.append(String.format("_You have not added any logs yet.\nUse the %s command to add a log._", CommandHandlerHelper.getCommandReference(CommandConstants.CommandLog)));
        else {
            logsPage.stream().sorted(Comparator.comparing(ExerciseLog::getCreated)).forEach(log -> {
                addGeneralLogString(logsDescriptionBuilder, log);
                addAchievementsString(logsDescriptionBuilder, log.getAchievements().stream().map(UserAchievement::getAchievement).toList());
                logsDescriptionBuilder.append(CommandHandlerHelper.addCongratulationsString(log, 2, false));
                logsDescriptionBuilder.append("\n");
            });
        }

        var descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(String.format("Name: %s\n", session.getName()));
        descriptionBuilder.append(String.format("Started: %s\n", CommandHandlerHelper.discordFormatDateTime(session.getCreated())));
        descriptionBuilder.append(String.format("Status: %s\n", session.getStatus().getDisplayName()));
        if (session.getRating() != null) descriptionBuilder.append(String.format("Rating: %s\n", CommandHandlerHelper.getRatingString(session.getRating())));
        descriptionBuilder.append(String.format("Logs: %d\n", logs.size()));
        descriptionBuilder.append(String.format("### Logs\n%s", logsDescriptionBuilder));

        var embed = EmbedCreateSpec.builder()
                .title(SessionCommandsConstants.sessionViewEmbedTitle)
                .description(descriptionBuilder.toString())
                .color(Color.GREEN);

        var commandName = switch (commandType) {
            case CREATE -> {
                embed.title(SessionCommandsConstants.sessionCreateEmbedTitle);
                yield CommandConstants.CommandSessionsCreate;
            }
            case STOP -> {
                embed.title(SessionCommandsConstants.sessionStopEmbedTitle);
                yield CommandConstants.CommandSessionsStop;
            }
            default -> CommandConstants.CommandSessionsViewOne;
        };

        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, logsPage.getTotalPages(), commandName, session.getId().toString());
        return InteractionReplyEditSpec.builder().addEmbed(embed.build()).addComponent(paginationControls).build();
    }

    private static void addGeneralLogString(StringBuilder stringBuilder, ExerciseLog log) {
        var exercise = log.getExerciseType();
        var measurement = exercise.getMeasurementType();
        var amount = CommandHandlerHelper.formatDouble(log.getAmount());
        var time = CommandHandlerHelper.discordFormatTime(log.getCreated());
        stringBuilder.append(String.format("**#%d %s**\nAmount: %s %s\nCreated: %s\nXp earned: %d\n", exercise.getId(), exercise.getName(), amount, measurement.getShortName(), time, log.getEarnedXp()));
    }

    private static void addAchievementsString(StringBuilder stringBuilder, List<Achievement> achievements) {
        if (achievements.isEmpty()) return;
        stringBuilder.append("Achievements:\n");
        for (var achievement : achievements) {
            stringBuilder.append(itemSpacing).append(String.format("<:%s:%s> %s \n", achievement.getDisplayName(), EmojiRegistrarService.getEmojiId(achievement, false).asString(), achievement.getTitle()));
        }
    }
}
