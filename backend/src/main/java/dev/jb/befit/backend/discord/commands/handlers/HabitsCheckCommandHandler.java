package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitsCheckCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final HabitService habitService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsCheck;
    }

    @Override
    public Mono<ChatInputInteractionEvent> preExecute(ChatInputInteractionEvent event) {
        return event.deferReply().withEphemeral(false).then(Mono.just(event));
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var discordUser = event.getInteraction().getUser();
        var user = userService.getOrCreateDiscordUser(discordUser.getId());
        var habits = habitService.getHabitsForToday(user);

        if (habits.daily().isEmpty() && habits.weekly().isEmpty() && habits.monthly().isEmpty()) {
            return event.editReply("You have no habits").then();
        }

        var sendMessageMono = Mono.from(event.editReply("# Your habits report")).then();

        sendMessageMono = addAndSpreadHabits(event, sendMessageMono, habits.daily(), HabitTimeRange.DAILY);
        sendMessageMono = addAndSpreadHabits(event, sendMessageMono, habits.weekly(), HabitTimeRange.WEEKLY);
        sendMessageMono = addAndSpreadHabits(event, sendMessageMono, habits.monthly(), HabitTimeRange.MONTHLY);

        return sendMessageMono;
    }

    private Mono<Void> addAndSpreadHabits(ChatInputInteractionEvent event, Mono<Void> mono, List<Habit> habits, HabitTimeRange timeRange) {
        if (habits.isEmpty()) return mono;

        var channelMono = mono.then(event.getInteraction().getChannel());
        var seperatedHabits = splitHabitsIntoGroups(habits);

        var messages = new MessageCreateSpec[seperatedHabits.size()];
        for (int i = 0; i < seperatedHabits.size(); i++) {
            var habitGroup = seperatedHabits.get(i);
            var messageBuilder = MessageCreateSpec.builder();
            if (i == 0) {
                messageBuilder.content(getTimeRangeTitle(timeRange));
            }
            for (int j = 0; j < habitGroup.size(); j++) {
                var habit = habitGroup.get(j);
                messageBuilder.addComponent(
                        ActionRow.of(
                                Button.secondary(getButtonId(habit), ReactionEmoji.unicode("❌")),
                                Button.danger(getButtonId(habit)+"$text", habit.getName()).disabled()
                        )
                );
            }
            messages[i] = (messageBuilder.build());
        }
        return channelMono.flatMapMany(channel -> Flux.just(messages).flatMap(channel::createMessage)).then();
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
            case DAILY -> "## :pencil: Daily habits";
            case WEEKLY -> "## :pencil: Weekly habits";
            case MONTHLY -> "## :pencil: Monthly habits";
        };
    }

    private String getButtonId(Habit habit) {
        var date = LocalDate.now();
        return String.format("%s$%d/%d/%d/%d", CommandConstants.CommandHabitsCheck, habit.getId(), date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }
}
