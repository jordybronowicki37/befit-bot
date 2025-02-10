package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputAutoCompleteEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitAutoCompleteHandler extends DiscordChatInputAutoCompleteEventListener {
    private final HabitService habitService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.AutoCompletePropHabit;
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        var filter = CommandHandlerHelper.getAutocompleteOptionFilter(event);
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);

        var habits = habitService.searchHabit(user, filter, Pageable.ofSize(CommandConstants.SearchResultsSize).withPage(0));
        var suggestions = habits.stream()
                .limit(CommandConstants.SearchResultsSize)
                .map(habit -> ApplicationCommandOptionChoiceData.builder().name(habit.getName()).value(habit.getId()).build())
                .toList();
        return event.respondWithSuggestions(new ArrayList<>(suggestions));
    }
}
