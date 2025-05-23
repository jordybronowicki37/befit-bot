package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.exceptions.HabitNotFoundException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitsViewOneCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final HabitService habitService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsViewOne;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var habitId = CommandHandlerHelper.getOptionValueAsLong(subCommand, "habit");

        var userId = CommandHandlerHelper.getDiscordUserId(event);
        var user = userService.getOrCreateDiscordUser(userId);

        var habit = habitService.getHabitByUserAndId(user, habitId).orElseThrow(() -> new HabitNotFoundException(habitId));

        var amountOfCheckups = HabitHelper.getAmountOfHabitCheckUps(habit);
        var amountOfChecks = habit.getHabitLogs().size();
        var description = new StringBuilder();
        description.append(String.format("Name: %s", habit.getName()));
        description.append(String.format("\nTime range: %s", habit.getHabitTimeRange().name().toLowerCase()));
        description.append(String.format("\nCreated: %s", CommandHandlerHelper.discordFormatDateTime(habit.getCreated())));
        var nextCheck = HabitHelper.getNextCheckListTimeForTimeRange(habit.getHabitTimeRange());
        description.append(String.format("\nNext checkup: %s", CommandHandlerHelper.discordFormatDateTime(nextCheck)));
        description.append(String.format("\nTotal checkups: %s", HabitHelper.getAmountOfHabitCheckUps(habit)));
        description.append(String.format("\nTotal checks: %d", amountOfChecks));
        description.append(String.format("\nCheck percentage: %d%%", amountOfCheckups != 0 ? amountOfChecks * 100L / amountOfCheckups : 0));

        var embed = EmbedCreateSpec.builder()
                .title(":pencil: Your Habit")
                .description(description.toString())
                .build();
        return Mono.from(event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build())).then();
    }
}
