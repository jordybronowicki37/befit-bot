package dev.jb.befit.backend.discord.commands.handlers.exercises;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseTypeService;
import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
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
public class ExerciseRenameCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseTypeService exerciseService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandExercisesRename;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());

        var oldExerciseName = CommandHandlerHelper.getOptionValue(subCommand, CommandConstants.AutoCompletePropExerciseName);
        var newExerciseName = CommandHandlerHelper.getOptionValue(subCommand, "new-name");

        var oldName = exerciseService.getByName(oldExerciseName).orElseThrow(() -> new ExerciseNotFoundException(oldExerciseName)).getName();
        exerciseService.rename(oldExerciseName, newExerciseName);

        var embed = EmbedCreateSpec.builder()
                .title("Exercise Renamed")
                .description(String.format("Old name: %s\nNew name: %s", oldName, newExerciseName))
                .color(Color.GREEN)
                .build();

        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }
}
