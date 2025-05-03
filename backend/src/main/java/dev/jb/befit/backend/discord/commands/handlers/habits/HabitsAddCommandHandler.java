package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
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
public class HabitsAddCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final HabitService habitService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsAdd;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var habitName = CommandHandlerHelper.getOptionValue(subCommand, "name");
        var habitTimeRange = HabitTimeRange.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "time-range"));

        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);

        var habit = habitService.createHabit(user, habitName, habitTimeRange);

        var description = new StringBuilder();
        description.append(String.format("Name: %s", habit.getName()));
        description.append(String.format("\nTime range: %s", habit.getHabitTimeRange().name().toLowerCase()));
        var nextCheck = HabitHelper.getNextCheckListTimeForTimeRange(habitTimeRange);
        description.append(String.format("\nNext checkup: %s", CommandHandlerHelper.discordFormatDateTime(nextCheck)));

        var embed = EmbedCreateSpec.builder()
                .title(":pencil: Your new Habit")
                .description(description.toString())
                .build();
        return Mono.from(event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build())).then();
    }
}
