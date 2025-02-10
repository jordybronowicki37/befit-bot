package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputAutoCompleteEventListener;
import dev.jb.befit.backend.service.ExerciseSessionService;
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
public class SessionAutoCompleteHandler extends DiscordChatInputAutoCompleteEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.AutoCompletePropSession;
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        var filter = CommandHandlerHelper.getAutocompleteOptionFilter(event);
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);

        var sessions = exerciseSessionService.searchByNameAndUser(user, filter, Pageable.ofSize(CommandConstants.SearchResultsSize).withPage(0));
        var suggestions = sessions.stream()
                .limit(CommandConstants.SearchResultsSize)
                .map(session -> {
                    var name = String.format("%s - %s", session.getName(), CommandHandlerHelper.formatDate(session.getCreated().toLocalDate()));
                    return ApplicationCommandOptionChoiceData.builder().name(name).value(session.getId()).build();
                })
                .toList();
        return event.respondWithSuggestions(new ArrayList<>(suggestions));
    }
}
