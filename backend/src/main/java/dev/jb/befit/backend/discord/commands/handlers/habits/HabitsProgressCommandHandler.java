package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.data.models.HabitLog;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.visuals.HabitImageService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitsProgressCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final HabitImageService habitImageService;
    private final HabitService habitService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsProgress;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        var user = userService.getOrCreateDiscordUser(userId);
        var subCommand = CommandHandlerHelper.getSubCommand(event, getCommandNameFilter());
        var habitTimeRange = HabitTimeRange.valueOf(CommandHandlerHelper.getOptionValue(subCommand, "time-range"));

        var startDate = getStartDateForTimeRange(habitTimeRange);
        var amountPassed = getTimePassed(habitTimeRange, startDate);
        var accurateDateMeasureName = getTimeRangeName(habitTimeRange, amountPassed);
        var singleDateMeasureName = getTimeRangeName(habitTimeRange, 1);

        var logs = habitService.getHabitLogsFrom(user, habitTimeRange, startDate);
        var logsGrouped = logs.stream().collect(Collectors.groupingBy(HabitLog::getHabit));

        var description = new StringBuilder();
        description.append(String.format("Time passed: %d %s\n", amountPassed, accurateDateMeasureName));
        description.append(String.format("Amount of different habits: %d\n", logsGrouped.size()));
        description.append(String.format("Average amount of logs per %s: %s\n", singleDateMeasureName, new DecimalFormat("0.#").format((double) logs.size() / amountPassed)));

        if (logsGrouped.size() > 1) {
            var worstHabit = logsGrouped.entrySet().stream().min(Comparator.comparingInt(l -> l.getValue().size()));
            var bestHabit = logsGrouped.entrySet().stream().max(Comparator.comparingInt(l -> l.getValue().size()));
            worstHabit.ifPresent(habit -> description.append(String.format("Worst habit: %s\n", habit.getKey().getName())));
            bestHabit.ifPresent(habit -> description.append(String.format("Best habit: %s\n", habit.getKey().getName())));
        }

        var embed = EmbedCreateSpec.builder()
                .title(":bar_chart: Your habits progress")
                .description(description.toString())
                .color(Color.GREEN)
                .image("attachment://habits-chart.png");

        try {
            var habitsChartImage = habitImageService.getHabitReportChart(user, habitTimeRange, startDate);
            var inputStream = new FileInputStream(habitsChartImage);
            return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed.build()).addFile("habits-chart.png", inputStream).build()).then();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDate getStartDateForTimeRange(HabitTimeRange habitTimeRange) {
        return switch (habitTimeRange) {
            case DAILY -> LocalDate.now().minusDays(30);
            case WEEKLY -> LocalDate.now().minusWeeks(26);
            case MONTHLY -> LocalDate.now().minusMonths(12);
        };
    }

    private String getTimeRangeName(HabitTimeRange habitTimeRange, int amount) {
        return switch (habitTimeRange) {
            case DAILY -> amount > 1 ? "days" : "day";
            case WEEKLY -> amount > 1 ? "weeks" : "week";
            case MONTHLY -> amount > 1 ? "months" : "month";
        };
    }

    private Integer getTimePassed(HabitTimeRange habitTimeRange, LocalDate date) {
        var now = LocalDate.now();
        return switch (habitTimeRange) {
            case DAILY -> Math.toIntExact(ChronoUnit.DAYS.between(date, now));
            case WEEKLY -> Math.toIntExact(ChronoUnit.WEEKS.between(date, now));
            case MONTHLY -> Math.toIntExact(ChronoUnit.MONTHS.between(date, now));
        };
    }
}
