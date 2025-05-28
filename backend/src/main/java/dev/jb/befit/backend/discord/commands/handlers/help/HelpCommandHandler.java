package dev.jb.befit.backend.discord.commands.handlers.help;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.commands.exceptions.CommandNotFoundException;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import discord4j.common.JacksonResources;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields.Field;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpCommandHandler extends DiscordChatInputInteractionEventListener {
    private final ResourceLoader resourceLoader;
    private final JacksonResources d4jMapper = JacksonResources.create();

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandHelp;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var avatarUrl = event.getClient().getSelf().block().getAvatarUrl();
        var commandName = CommandHandlerHelper.getOptionalOptionValue(event, CommandConstants.AutoCompletePropCommand, null);
        var embed = commandName == null ? getGeneralEmbed(avatarUrl) : getCommandEmbed(avatarUrl, commandName);
        return event.editReply(InteractionReplyEditSpec.builder().addEmbed(embed).build()).then();
    }

    private EmbedCreateSpec getGeneralEmbed(String avatarUrl) {
        return EmbedCreateSpec.builder()
                .author("Befit bot", "https://github.com/jordybronowicki37/befit-bot", avatarUrl)
                .title("About this bot")
                .description("This bot helps you track your gym progress and motivates you into a better lifestyle.\n\n### Useful commands:")
                .addFields(
                        Field.of(CommandHandlerHelper.getCommandReference(CommandConstants.CommandLog), "Create a new log for an exercise. The more logs you create the better the bot can help you improve.", false),
                        Field.of(CommandHandlerHelper.getCommandReference(CommandConstants.CommandExercisesCreate), "Don't see your favourite exercise inside of our catalogue? Just add it and track your progress for it!", false),
                        Field.of(CommandHandlerHelper.getCommandReference(CommandConstants.CommandExercisesViewAll, CommandConstants.CommandExercisesViewMy, CommandConstants.CommandExercisesViewOne), "View all the exercises, your own exercises or detailed information about a single exercise.", false),
                        Field.of(CommandHandlerHelper.getCommandReference(CommandConstants.CommandGoalsAdd), "Set goals for you to reach and work towards them.", false),
                        Field.of(CommandHandlerHelper.getCommandReference(CommandConstants.CommandAchievements), "See which achievements you have completed and which are still locked.", false),
                        Field.of(CommandHandlerHelper.getCommandReference(CommandConstants.CommandMotivation), "If you are looking for motivation, just use this command and get some.", false)
                )
                .color(Color.GRAY)
                .build();
    }

    private EmbedCreateSpec getCommandEmbed(String avatarUrl, String commandName) {
        var command = getHelpCommandResource(commandName);
        if (command == null) throw new CommandNotFoundException();

        var fullCommand = new StringBuilder("`").append(commandName);
        for (var argument : command.arguments()) {
            fullCommand.append(' ');
            if (!argument.required()) fullCommand.append('?');
            fullCommand.append(String.format("{%s}", argument.name()));
        }
        fullCommand.append('`');

        var description = new StringBuilder(String.format("Format: %s", fullCommand));
        description.append(String.format("\n\n%s", command.description()));
        if (!command.arguments().isEmpty()) description.append("\n\n### Arguments:");

        var fields = command.arguments()
                .stream()
                .map(a -> Field.of(a.name(), a.description(), false))
                .toList();

        var embed = EmbedCreateSpec.builder()
                .author("Befit bot", "https://github.com/jordybronowicki37/befit-bot", avatarUrl)
                .title(CommandHandlerHelper.getCommandReference(commandName))
                .description(description.toString())
                .fields(fields)
                .color(Color.GRAY);

        if (command.image() != null) embed.image(command.image());

        return embed.build();
    }

    public HelpCommandResource getHelpCommandResource(String commandName) {
        return getHelpResource().commands().stream().filter(c -> c.command().equals(commandName)).findFirst().orElse(null);
    }

    public HelpResource getHelpResource() {
        try {
            var resource = resourceLoader.getResource("classpath:help.json");
            return d4jMapper.getObjectMapper().readValue(resource.getContentAsString(StandardCharsets.UTF_8), HelpResource.class);
        } catch (IOException e) {
            return null;
        }
    }

    private static String capitalizeFirstLetter(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
