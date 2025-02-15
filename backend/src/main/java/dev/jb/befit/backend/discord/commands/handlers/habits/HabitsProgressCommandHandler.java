package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.visuals.HabitImageService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitsProgressCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final HabitImageService habitImageService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsProgress;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var habitTimeRange = HabitTimeRange.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "time-range"));

        var startDate = switch (habitTimeRange) {
            case DAILY -> LocalDate.now().minusDays(30);
            case WEEKLY -> LocalDate.now().minusWeeks(26);
            case MONTHLY -> LocalDate.now().minusMonths(12);
        };

        try {
            var habitsChartImage = habitImageService.getHabitReportChart(user, habitTimeRange, startDate);
            var inputStream = new FileInputStream(habitsChartImage);
            return event.editReply(InteractionReplyEditSpec.builder().addFile("habits-chart.png", inputStream).build()).then();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
