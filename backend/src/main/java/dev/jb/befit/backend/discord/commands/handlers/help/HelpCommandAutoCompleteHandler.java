package dev.jb.befit.backend.discord.commands.handlers.help;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputAutoCompleteEventListener;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpCommandAutoCompleteHandler extends DiscordChatInputAutoCompleteEventListener {
    private final HelpCommandHandler helpCommandHandler;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.AutoCompletePropCommand;
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        var filter = CommandHandlerHelper.getAutocompleteOptionFilter(event);
        var commands = helpCommandHandler.getHelpResource().commands();
        var suggestions = commands.stream()
                .sorted(Comparator.comparing(HelpCommandResource::command))
                .filter(c -> c.command().contains(filter))
                .limit(CommandConstants.SearchResultsSize)
                .map(c -> ApplicationCommandOptionChoiceData.builder().name(c.command()).value(c.command()).build())
                .toList();
        return event.respondWithSuggestions(new ArrayList<>(suggestions));
    }
}
