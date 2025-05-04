package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseSessionService;
import dev.jb.befit.backend.service.UserService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionsViewAllCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ExerciseSessionService exerciseSessionService;
    private final UserService userService;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandSessionsViewAll;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var userId = CommandHandlerHelper.getDiscordUserId(event);
        return event.editReply(getReplyEditSpec(userId, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var user = userService.getOrCreateDiscordUser(userId);
        var sessions = exerciseSessionService.getAllByUser(user, Pageable.ofSize(CommandConstants.PageSize).withPage(page));

        var embed = EmbedCreateSpec.builder()
                .title(":notepad_spiral: Your sessions")
                .fields(sessions
                        .stream()
                        .map(session -> {
                            var descriptionBuilder = new StringBuilder();
                            descriptionBuilder.append(String.format("Started: %s\n", CommandHandlerHelper.discordTimeAgoText(session.getCreated())));
                            descriptionBuilder.append(String.format("Status: %s\n", session.getStatus().getDisplayName()));
                            if (session.getRating() != null) descriptionBuilder.append(String.format("Rating: %s\n", CommandHandlerHelper.getRatingString(session.getRating())));
                            descriptionBuilder.append(String.format("Logs: %s\n", session.getExerciseLogs().size()));
                            return EmbedCreateFields.Field.of(
                                    session.getName(),
                                    descriptionBuilder.toString(),
                                    false);
                        })
                        .toList())
                .color(Color.GREEN)
                .build();

        var paginationControls = CommandHandlerHelper.getPaginationComponent(page, sessions.getTotalPages(), getCommandNameFilter());
        return InteractionReplyEditSpec.builder().addEmbed(embed).addComponent(paginationControls).build();
    }
}
