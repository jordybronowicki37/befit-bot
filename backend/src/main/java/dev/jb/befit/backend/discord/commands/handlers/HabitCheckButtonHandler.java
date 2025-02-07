package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.listeners.DiscordButtonInteractionEventListener;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.UserService;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.InteractionReplyEditSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitCheckButtonHandler extends DiscordButtonInteractionEventListener {
    private final UserService userService;
    private final HabitService habitService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHabitsCheck;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ButtonInteractionEvent event) {
        var userId = event.getInteraction().getUser().getId();
        var user = userService.getOrCreateDiscordUser(userId);

        var customId = event.getCustomId();
        var data = customId.split("\\$")[1].split("/");
        var habitId = Long.parseLong(data[0]);

        var dateYear = Integer.parseInt(data[1]);
        var dateMonth = Integer.parseInt(data[2]);
        var dateDay = Integer.parseInt(data[3]);
        var date = LocalDate.of(dateYear, dateMonth, dateDay);

        var habitLog = habitService.flipHabitCompleted(user, habitId, date);

        var originalMessage = event.getMessage().get();
        var newMessage = InteractionReplyEditSpec.builder().contentOrNull(originalMessage.getContent());
        originalMessage.getComponents().forEach(component -> {
            var button = component.getChildren().get(0);
            var habitComponent = component.getChildren().get(1);
            if (!button.getData().customId().get().equals(customId)) {
                newMessage.addComponent(component);
                return;
            }
            var isCompleted = habitLog != null;
            var newEmoji = isCompleted ? "✅" : "❌";
            var habitText = habitComponent.getData().label().get();
            var newTextLabel = Button.success(customId + "$text", habitText).disabled();
            if (!isCompleted) newTextLabel = Button.danger(customId + "$text", habitText).disabled();
            newMessage.addComponent(
                    ActionRow.of(
                            Button.secondary(customId, ReactionEmoji.unicode(newEmoji)),
                            newTextLabel
                    )
            );
        });

        return event.editReply(newMessage.build()).then();
    }
}
