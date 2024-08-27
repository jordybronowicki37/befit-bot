package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.listeners.DiscordChatInputAutoCompleteEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseTypeAutoCompleteHandler extends DiscordChatInputAutoCompleteEventListener {
    private final ExerciseTypeService exerciseTypeService;

    @Override
    public String getCommandNameFilter() {
        return "exercise-name";
    }

    @Override
    public Mono<Void> execute(ChatInputAutoCompleteEvent event) {
        var filter = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
        var exercises = exerciseTypeService.getFiltered(filter);
        var suggestions = new ArrayList<ApplicationCommandOptionChoiceData>();
        exercises.stream().limit(25).forEach(e -> suggestions.add(ApplicationCommandOptionChoiceData.builder().name(e.getName()).value(e.getName()).build()));
        return event.respondWithSuggestions(suggestions);
    }
}