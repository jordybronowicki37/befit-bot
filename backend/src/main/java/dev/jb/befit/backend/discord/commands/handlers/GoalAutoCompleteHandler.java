package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.GoalStatus;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputAutoCompleteEventListener;
import dev.jb.befit.backend.service.GoalService;
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
public class GoalAutoCompleteHandler extends DiscordChatInputAutoCompleteEventListener {
    private final GoalService goalService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.AutoCompletePropGoal;
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        var filter = CommandHandlerHelper.getAutocompleteOptionFilter(event).toLowerCase();
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var goals = goalService.getAllUserGoals(user, GoalStatus.ACTIVE);

        var suggestions = new ArrayList<ApplicationCommandOptionChoiceData>();
        goals.stream()
                .filter(g -> g.getExerciseType().getName().toLowerCase().contains(filter))
                .limit(CommandConstants.SearchResultsSize)
                .forEach(g -> suggestions.add(
                        ApplicationCommandOptionChoiceData.builder()
                                .name(String.format("%s - %s %s", g.getExerciseType().getName(), CommandHandlerHelper.formatDouble(g.getAmount()), g.getExerciseType().getMeasurementType().getShortName()))
                                .value(g.getId())
                                .build()
                ));
        return event.respondWithSuggestions(suggestions);
    }
}
