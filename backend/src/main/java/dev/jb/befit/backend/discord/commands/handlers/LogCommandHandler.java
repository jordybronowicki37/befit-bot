package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.discord.listeners.DiscordEventListener;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.MotivationalService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogCommandHandler implements DiscordEventListener<ChatInputInteractionEvent> {
    private final ExerciseLogService logService;
    private final UserService userService;
    private final MotivationalService motivationalService;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        if (!event.getCommandName().equals("log")) return Mono.empty();

        var exerciseName = event.getOption("exercise-name").orElseThrow().getValue().orElseThrow().asString();
        var exerciseAmount = Math.toIntExact(event.getOption("amount").orElseThrow().getValue().orElseThrow().asLong());
        var userId = event.getInteraction().getUser().getId();

        event.deferReply().block();

        var user = userService.getOrCreateDiscordUser(userId);
        var exerciseLog = logService.create(user, exerciseName, exerciseAmount);
        var exerciseType = exerciseLog.getExerciseType();
        var allExerciseLogs = logService.getAllByUserIdAndExerciseName(user, exerciseName);

        var descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(String.format("Exercise: #%d %s\n", exerciseType.getId(), exerciseType.getName()));
        descriptionBuilder.append(String.format("Log: #%d\n", allExerciseLogs.size()));
        descriptionBuilder.append(String.format("Value: %d%s\n", exerciseLog.getAmount(), exerciseType.getMeasurementType()));
        if (allExerciseLogs.size() >= 2) {
            var previousLog = allExerciseLogs.get(allExerciseLogs.size() - 2);
            descriptionBuilder.append(String.format("Last: %d%s\n", previousLog.getAmount(), exerciseType.getMeasurementType()));
        }

        var exerciseAmounts = new ArrayList<>(allExerciseLogs.stream().map(ExerciseLog::getAmount).toList());
        exerciseAmounts.remove(exerciseLog.getAmount());
        if (exerciseAmounts.stream().max(Integer::compareTo).orElse(0) < exerciseLog.getAmount()) {
            descriptionBuilder.append("\n:rocket: NEW PR REACHED!");
        }

        var embed = EmbedCreateSpec.builder()
                .title(":muscle: Logged workout")
                .description(descriptionBuilder.toString())
                .footer(motivationalService.getRandomPositiveReinforcement(), null)
                .color(Color.GREEN)
                .build();
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
