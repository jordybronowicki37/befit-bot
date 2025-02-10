package dev.jb.befit.backend.discord.commands.handlers;

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
        var userId = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(userId, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var user = userService.getOrCreateDiscordUser(userId);
        var habitsPage = habitService.getHabitsByUser(user, Pageable.ofSize(CommandConstants.PageSize).withPage(page));
        var embed = EmbedCreateSpec.builder()
                .title(":pencil: Your Habits")
                .fields(
                        habitsPage.stream().map(habit -> {
                            var nextCheckupDate = CommandHandlerHelper.getNextCheckListTimeForTimeRange(habit.getHabitTimeRange());
                            var amountOfCheckups = CommandHandlerHelper.getAmountOfHabitCheckUps(habit);
                            var amountOfChecks = habit.getHabitLogs().size();
                            var description = new StringBuilder();
                            description.append(String.format("\nTime range: %s", habit.getHabitTimeRange().name().toLowerCase()));
                            description.append(String.format("\nCreated: %s", CommandHandlerHelper.discordFormatDateTime(habit.getCreated())));
                            description.append(String.format("\nNext checkup: %s", CommandHandlerHelper.discordFormatDateTime(nextCheckupDate)));
                            description.append(String.format("\nTotal checkups: %s", amountOfCheckups));
                            description.append(String.format("\nTotal checks: %d", amountOfChecks));
                            if (amountOfCheckups != 0) description.append(String.format("\nCheck percentage: %d%%", amountOfChecks * 100L / amountOfCheckups));
                            return EmbedCreateFields.Field.of(habit.getName(), description.toString(), false);
                        }).toList()
                )
                .build();
        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, habitsPage.getTotalPages(), getCommandNameFilter());
        return InteractionReplyEditSpec.builder().addEmbed(embed).addComponent(paginationControls).build();
    }
}
