package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.data.models.DiscordUser;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.data.models.WebUser;
import dev.jb.befit.backend.discord.commands.exceptions.OptionNotFoundException;
import dev.jb.befit.backend.discord.commands.exceptions.ValueNotFoundException;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND;
import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND_GROUP;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandHandlerHelper {
    public static String getCommandName(ChatInputInteractionEvent event) {
        var actualCommandNameBuilder = new StringBuilder();
        actualCommandNameBuilder.append(event.getCommandName());

        if (!event.getOptions().isEmpty()) {
            var nestedOption = event.getOptions().get(0);
            var nestedOptionIsSubCommand = nestedOption.getType().equals(SUB_COMMAND);
            var nestedOptionIsSubCommandGroup = nestedOption.getType().equals(SUB_COMMAND_GROUP);

            if (nestedOptionIsSubCommand || nestedOptionIsSubCommandGroup) {
                actualCommandNameBuilder.append(" ").append(nestedOption.getName());
            }

            if (nestedOptionIsSubCommandGroup && !nestedOption.getOptions().isEmpty()) {
                actualCommandNameBuilder.append(" ").append(nestedOption.getOptions().get(0).getName());
            }
        }

        return actualCommandNameBuilder.toString();
    }

    public static boolean checkCommandName(ChatInputInteractionEvent event, String commandName) {
        return getCommandName(event).equals(commandName);
    }

    public static ApplicationCommandInteractionOption getSubCommand(ChatInputInteractionEvent event, String commandName) {
        var splitCommand = commandName.split(" ");

        return switch (splitCommand.length) {
            case 2 -> getOption(event, splitCommand[1]);
            case 3 -> getOption(getOption(event, splitCommand[1]), splitCommand[2]);
            default -> null;
        };
    }

    public static ApplicationCommandInteractionOption getOption(ApplicationCommandInteractionOption subCommand, String optionName) {
        return subCommand.getOption(optionName).orElseThrow(() -> new OptionNotFoundException(optionName));
    }

    public static ApplicationCommandInteractionOption getOption(ChatInputInteractionEvent event, String optionName) {
        return event.getOption(optionName).orElseThrow(() -> new OptionNotFoundException(optionName));
    }

    public static String getOptionValue(ApplicationCommandInteractionOption subCommand, String optionName) {
        return getOptionValueRaw(getOption(subCommand, optionName)).asString();
    }

    public static String getOptionValue(ChatInputInteractionEvent event, String optionName) {
        return getOptionValueRaw(getOption(event, optionName)).asString();
    }

    public static Long getOptionValueAsLong(ApplicationCommandInteractionOption subCommand, String optionName) {
        return getOptionValueRaw(getOption(subCommand, optionName)).asLong();
    }

    public static Long getOptionValueAsLong(ChatInputInteractionEvent event, String optionName) {
        return getOptionValueRaw(getOption(event, optionName)).asLong();
    }

    public static Double getOptionValueAsDouble(ApplicationCommandInteractionOption subCommand, String optionName) {
        return getOptionValueRaw(getOption(subCommand, optionName)).asDouble();
    }

    public static Double getOptionValueAsDouble(ChatInputInteractionEvent event, String optionName) {
        return getOptionValueRaw(getOption(event, optionName)).asDouble();
    }

    public static Integer getOptionValueAsInt(ApplicationCommandInteractionOption subCommand, String optionName) {
        return Math.toIntExact(getOptionValueAsLong(subCommand, optionName));
    }

    public static Integer getOptionValueAsInt(ChatInputInteractionEvent event, String optionName) {
        return Math.toIntExact(getOptionValueAsLong(event, optionName));
    }

    public static ApplicationCommandInteractionOptionValue getOptionValueRaw(ApplicationCommandInteractionOption option) {
        return option.getValue().orElseThrow(() -> new ValueNotFoundException(option.getName()));
    }

    public static String getOptionalOptionValue(ApplicationCommandInteractionOption subCommand, String optionName, String defaultValue) {
        return subCommand.getOption(optionName)
                .map(v -> v.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse(defaultValue))
                .orElse(defaultValue);
    }

    public static String getOptionalOptionValue(ChatInputInteractionEvent event, String optionName, String defaultValue) {
        return event.getOption(optionName)
                .map(v -> v.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse(defaultValue))
                .orElse(defaultValue);
    }

    public static String getAutocompleteOptionFilter(ChatInputAutoCompleteEvent event) {
        return event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::getRaw)
                .orElse("");
    }

    public static String formatDouble(Double num) {
        var format = new DecimalFormat("0.###");
        return format.format(num);
    }

    public static String getLeaderboardValue(Integer position) {
        return switch (position) {
            case 1 -> ":first_place:";
            case 2 -> ":second_place:";
            case 3 -> ":third_place:";
            default -> position + "th";
        };
    }

    public static String getUserStringValue(User user) {
        if (user instanceof DiscordUser discordUser) return String.format("<@%s>", discordUser.getDiscordId().asString());
        if (user instanceof WebUser webUser) return String.format("`%s`", webUser.getUsername());
        return "`unknown`";
    }

    public static int getAmountOfPages(int items, int pageSize) {
        return (int) Math.ceil((double) items / pageSize);
    }

    public static ActionRow getPaginationComponent(int pageNumber, int amountOfPages, String commandName) {
        var previousButton = Button.secondary(String.format("%s$%d", commandName, pageNumber-1), ReactionEmoji.unicode("⬅"));
        if (pageNumber <= 0 || amountOfPages == 0) previousButton = previousButton.disabled();
        var reloadButton = Button.secondary(String.format("%s$%d", commandName, pageNumber), String.format("%d/%d", amountOfPages == 0 ? 0 : pageNumber+1, amountOfPages));
        var nextButton = Button.secondary(String.format("%s$%d", commandName, pageNumber+1), ReactionEmoji.unicode("➡"));
        if (pageNumber == amountOfPages - 1 || amountOfPages == 0) nextButton = nextButton.disabled();

        return ActionRow.of(previousButton, reloadButton, nextButton);
    }

    public static String timeAgoText(LocalDateTime date) {
        var epochSeconds = date.toEpochSecond(ZoneOffset.UTC);
        return String.format("<t:%d:R>", epochSeconds);
    }

    public static String formatDate(LocalDateTime date) {
        var epochSeconds = date.toEpochSecond(ZoneOffset.UTC);
        return String.format("<t:%d:d>", epochSeconds);
    }

    public static String formatTime(LocalDateTime time) {
        var epochSeconds = time.toEpochSecond(ZoneOffset.UTC);
        return String.format("<t:%d:t>", epochSeconds);
    }

    public static String formatDateTime(LocalDateTime date) {
        var epochSeconds = date.toEpochSecond(ZoneOffset.UTC);
        return String.format("<t:%d:f>", epochSeconds);
    }
}
