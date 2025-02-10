package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitsRemoveCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final HabitService habitService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsRemove;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var habitId = CommandHandlerHelper.getOptionValueAsLong(subCommand, "habit");

        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);

        habitService.removeHabit(user, habitId);

        return Mono.from(event.editReply("# Habit successfully removed")).then();
    }
}
