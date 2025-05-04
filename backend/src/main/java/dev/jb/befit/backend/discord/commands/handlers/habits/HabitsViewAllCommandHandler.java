package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitsViewAllCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final HabitService habitService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsViewAll;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        assert subCommand != null;
        var habitTimeRangeValue = CommandHandlerHelper.getOptionalOptionValue(subCommand, "time-range", null);
        HabitTimeRange habitTimeRange = null;
        if (habitTimeRangeValue != null) {
            habitTimeRange = HabitTimeRange.valueOf(habitTimeRangeValue);
        }
        return event.editReply(getReplyEditSpec(userId, 0, habitTimeRange)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page, HabitTimeRange habitTimeRange) {
        var user = userService.getOrCreateDiscordUser(userId);

        Page<Habit> habitsPage;
        if (habitTimeRange != null) {
            habitsPage = habitService.getHabitsByUserAndTimeRange(user, habitTimeRange, Pageable.ofSize(CommandConstants.PageSize).withPage(page));
        } else {
            habitsPage = habitService.getHabitsByUser(user, Pageable.ofSize(CommandConstants.PageSize).withPage(page));
        }

        var embed = EmbedCreateSpec.builder()
                .title(":pencil: Your Habits")
                .fields(
                        habitsPage.stream().map(habit -> {
                            var nextCheckupDate = HabitHelper.getNextCheckListTimeForTimeRange(habit.getHabitTimeRange());
                            var amountOfCheckups = HabitHelper.getAmountOfHabitCheckUps(habit);
                            var amountOfChecks = habit.getHabitLogs().size();
                            var description = new StringBuilder();
                            description.append(String.format("\nTime range: %s", habit.getHabitTimeRange().name().toLowerCase()));
                            description.append(String.format("\nCreated: %s", CommandHandlerHelper.discordTimeAgoText(habit.getCreated())));
                            description.append(String.format("\nNext checkup: %s", CommandHandlerHelper.discordFormatDateTime(nextCheckupDate)));
                            description.append(String.format("\nCheck percentage: %d%%", amountOfCheckups != 0 ? amountOfChecks * 100L / amountOfCheckups : 0));
                            return EmbedCreateFields.Field.of(habit.getName(), description.toString(), false);
                        }).toList()
                );
        if (habitsPage.isEmpty()) embed.description("You have no habits yet, try adding some!");
        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, habitsPage.getTotalPages(), getCommandNameFilter(), String.valueOf(habitTimeRange));
        return InteractionReplyEditSpec.builder().addEmbed(embed.build()).addComponent(paginationControls).build();
    }
}
