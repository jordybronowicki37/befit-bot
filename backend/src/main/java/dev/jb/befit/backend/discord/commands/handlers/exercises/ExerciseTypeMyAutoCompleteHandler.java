package dev.jb.befit.backend.discord.commands.handlers.exercises;

import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputAutoCompleteEventListener;
import dev.jb.befit.backend.service.ExerciseRecordService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseTypeMyAutoCompleteHandler extends DiscordChatInputAutoCompleteEventListener {
    private final ExerciseRecordService exerciseRecordService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.AutoCompletePropMyExerciseName;
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        var filter = CommandHandlerHelper.getAutocompleteOptionFilter(event);
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        var user = userService.getOrCreateDiscordUser(userId);
        var records = exerciseRecordService.getFiltered(user, filter);
        var suggestions = records.stream()
                .limit(CommandConstants.SearchResultsSize)
                .map(ExerciseRecord::getExerciseType)
                .map(e ->
                        ApplicationCommandOptionChoiceData.builder()
                                .name(String.format("#%d %s - %s %s", e.getId(), e.getName(), e.getMeasurementType().getLongName(), e.getGoalDirection().equals(GoalDirection.INCREASING) ? "⇑" : "️⇓"))
                                .value(String.format("#%d", e.getId()))
                                .build()
                )
                .toList();
        return event.respondWithSuggestions(new ArrayList<>(suggestions));
    }
}
