package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.UserAchievement;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.exceptions.InvalidValueException;
import dev.jb.befit.backend.discord.listeners.DiscordButtonInteractionEventListener;
import dev.jb.befit.backend.discord.registration.EmojiRegistrarService;
import dev.jb.befit.backend.service.ExerciseSessionService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionComponent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRateButtonHandler extends DiscordButtonInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsRate;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var optionsSplit = event.getCustomId().split("\\$");
        var rating = Integer.parseInt(optionsSplit[2]);
        if (rating < 1 || rating > 5) throw new InvalidValueException("Rating", String.valueOf(rating), "Rating must be from 1 to 5");
        var sessionId = Long.parseLong(optionsSplit[1]);
        var session = exerciseSessionService.updateRating(user, sessionId, rating);

        var replySpec = InteractionReplyEditSpec.builder()
                .contentOrNull(String.format("# :notepad_spiral: Rate your session\n%s", getRatingMessage(rating)))
                .addComponent(getRatingRow(session))
                .addComponent(getExtendSessionRow(session))
                .addEmbed(getSessionRecap(session));

        return event
                .editReply(replySpec.build())
                .flatMap(m -> Mono.just(exerciseSessionService.updateMessage(user, sessionId, m.getId())))
                .then();
    }

    public static EmbedCreateSpec getSessionRecap(ExerciseSession session) {
        var achievements = new LinkedList<Achievement>();
        var prsImproved = 0;
        var goalsCompleted = 0;
        var newExercisedStarted = 0;
        var experienceEarned = 0L;

        for (var log : session.getExerciseLogs()) {
            achievements.addAll(log.getAchievements().stream().map(UserAchievement::getAchievement).toList());
            if (log.isPrImproved()) prsImproved++;
            if (log.isGoalReached()) goalsCompleted++;
            if (log.isFirstLogOfExercise()) newExercisedStarted++;
            experienceEarned += log.getEarnedXp();
        }

        var description = new StringBuilder();
        description.append(String.format("Name: %s\n", session.getName()));
        description.append(String.format("Logs added: %d\n", session.getExerciseLogs().size()));
        description.append(String.format("Experience earned: %d\n", experienceEarned));
        if (prsImproved > 0) description.append(String.format("Personal records improved: %d\n", prsImproved));
        if (goalsCompleted > 0) description.append(String.format("Goals completed: %d\n", goalsCompleted));
        if (newExercisedStarted > 0) description.append(String.format("New exercises started: %d\n", newExercisedStarted));
        if (!achievements.isEmpty()) {
            description.append("Achievements completed: ");
            for (var achievement : achievements) {
                description.append(String.format("<:%s:%s> ", achievement.getDisplayName(), EmojiRegistrarService.getEmojiId(achievement, false).asString()));
            }
            description.append('\n');
        }

        var embed = EmbedCreateSpec
                .builder()
                .title("Session recap")
                .description(description.toString());

        return embed.build();
    }

    public static ActionRow getRatingRow(ExerciseSession session) {
        var selectedStarEmoji = ReactionEmoji.unicode("\uD83C\uDF1F");
        var unselectedStarEmoji = ReactionEmoji.unicode("⭐");
        var components = new ArrayList<ActionComponent>();

        for (int i = 0; i < 5; i++) {
            var customId = String.format("%s$%d$%d", CommandConstants.CommandSessionsRate, session.getId(), i+1);
            if (session.getRating() == null || session.getRating() <= i) {
                components.add(Button.secondary(customId, unselectedStarEmoji));
            }
            else {
                components.add(Button.success(customId, selectedStarEmoji));
            }
        }

        return ActionRow.of(components);
    }

    public static ActionRow getExtendSessionRow(ExerciseSession session) {
        return ActionRow.of(
                Button.secondary(String.format("%s$%d", CommandConstants.CommandSessionsExtend, session.getId()), "Not done yet? Extend session")
        );
    }

    public static String getRatingMessage(Integer rating) {
        return switch (rating) {
            case 1 -> "You'll get 'em next time!";
            case 2 -> "Keep going! Next session will be much better.";
            case 3 -> "That was pretty good. Next time will be much better!";
            case 4 -> "That was a good session! Keep this going!";
            case 5 -> "That was an amazing session! I'm proud of you.";
            default -> "Error, this rating is not valid! How did you achieve this?";
        };
    }
}
