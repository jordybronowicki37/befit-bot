package dev.jb.befit.backend.discord.jobs;

import dev.jb.befit.backend.data.models.DiscordUser;
import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.dto.HabitsByTimeRange;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitJobController {
    private final GatewayDiscordClient client;
    private final HabitService habitService;
    private final UserService userService;

    @Transactional
    @Scheduled(cron = "0 0 19 * * *")
    public void sendAllHabitReports() {
        var users = userService.getAllUsers();
        users.forEach(user -> {
            if (!(user instanceof DiscordUser discordUser)) return;
            var habits = habitService.getHabitsForToday(discordUser);
            if (habits.isEmpty()) return;
            sendHabitReport(discordUser, habits).block();
        });
    }

    public Mono<Void> sendHabitReport(DiscordUser discordUser, HabitsByTimeRange habits) {
        return Mono
                .from(client.getUserById(discordUser.getDiscordId()))
                .flatMap(User::getPrivateChannel)
                .flatMap(privateChannel -> sendHabitsChecklistToChannel(Mono.just(privateChannel), Mono.empty(), habits))
                .then();
    }

    public static Mono<Void> sendHabitsChecklistToChannel(Mono<MessageChannel> channel, Mono<Void> sendMessageMono, HabitsByTimeRange habits) {
        if (habits.isEmpty()) {
            return sendMessageMono.then(channel.flatMap(c -> c.createMessage("# :pencil: Your habits checklist\nYou have not set up any habits.")).then());
        }
        sendMessageMono = sendMessageMono.then(channel.flatMap(c -> c.createMessage("# :pencil: Your habits checklist\nCheck the habits that you have completed over the given time range.")).then());
        sendMessageMono = addAndSpreadHabits(channel, sendMessageMono, habits.daily(), HabitTimeRange.DAILY);
        sendMessageMono = addAndSpreadHabits(channel, sendMessageMono, habits.weekly(), HabitTimeRange.WEEKLY);
        sendMessageMono = addAndSpreadHabits(channel, sendMessageMono, habits.monthly(), HabitTimeRange.MONTHLY);
        return sendMessageMono;
    }

    private static Mono<Void> addAndSpreadHabits(Mono<MessageChannel> channelMono, Mono<Void> mono, List<Habit> habits, HabitTimeRange timeRange) {
        if (habits.isEmpty()) return mono;

        var seperatedHabits = splitHabitsIntoGroups(habits);

        var messages = new MessageCreateSpec[seperatedHabits.size()];
        for (int i = 0; i < seperatedHabits.size(); i++) {
            var habitGroup = seperatedHabits.get(i);
            var messageBuilder = MessageCreateSpec.builder();
            if (i == 0) {
                messageBuilder.content(getTimeRangeTitle(timeRange));
            }
            for (Habit habit : habitGroup) {
                messageBuilder.addComponent(
                        ActionRow.of(
                                Button.secondary(getButtonId(habit), ReactionEmoji.unicode("❌")),
                                Button.danger(getButtonId(habit) + "$text", habit.getName()).disabled()
                        )
                );
            }
            messages[i] = (messageBuilder.build());
        }
        return mono.then(channelMono.flatMapMany(channel -> Flux.just(messages).flatMap(channel::createMessage)).then());
    }

    private static List<List<Habit>> splitHabitsIntoGroups(List<Habit> habits) {
        List<List<Habit>> seperatedHabits = new ArrayList<>();
        List<Habit> habitGroup = new ArrayList<>();
        for (int i = 0; i < habits.size(); i++) {
            if (i % 5 == 0) {
                habitGroup = new ArrayList<>();
                seperatedHabits.add(habitGroup);
            }
            var habit = habits.get(i);
            habitGroup.add(habit);
        }
        return seperatedHabits;
    }

    private static String getTimeRangeTitle(HabitTimeRange timeRange) {
        return switch (timeRange) {
            case DAILY -> "## Daily habits\nHow was your day today? I hope that you had some time to complete today's habits.";
            case WEEKLY -> "## Weekly habits\nThe week has come to an end. Let's check off those weekly habits.";
            case MONTHLY -> "## Monthly habits\nLet's reflect on the past month.";
        };
    }

    private static String getButtonId(Habit habit) {
        var date = LocalDate.now();
        return String.format("%s$action$%d/%d/%d/%d", CommandConstants.CommandHabitsCheck, habit.getId(), date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }
}
