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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND;
import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND_GROUP;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandHandlerHelper {
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
}
