package dev.jb.befit.backend.discord.commands.handlers.habits;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.jobs.HabitJobController;
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

        if (habits.isEmpty()) {
            return event.editReply("You have not set up any habits.").then();
        }

        var sendMessageMono = Mono.from(event.deleteReply()).then();
        var channelMono = event.getInteraction().getChannel();

        sendMessageMono = HabitJobController.sendHabitsChecklistToChannel(channelMono, sendMessageMono, habits);

        return sendMessageMono;
    }
}
