package dev.jb.befit.backend.discord.commands;

import dev.jb.befit.backend.data.models.DiscordUser;
import dev.jb.befit.backend.data.models.ExerciseLog;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.data.models.WebUser;
import dev.jb.befit.backend.discord.commands.exceptions.OptionNotFoundException;
import dev.jb.befit.backend.discord.commands.exceptions.ValueNotFoundException;
import dev.jb.befit.backend.discord.registration.CommandRegistrarService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND;
import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND_GROUP;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandHandlerHelper {
    private static String timeFormat;
    private static String dateFormat;

    @Value("${befit.timeFormat}")
    private void setTimeFormat(String timeFormat) {
        CommandHandlerHelper.timeFormat = timeFormat;
    }

    @Value("${befit.dateFormat}")
    private void setDateFormat(String dateFormat) {
        CommandHandlerHelper.dateFormat = dateFormat;
    }

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

    public static List<ApplicationCommandInteractionOption> getOptions(ChatInputInteractionEvent event, String commandName) {
        if (!commandName.contains(" ")) {
            return event.getOptions();
        }
        else {
            var subCommand = getSubCommand(event, commandName);
            if (subCommand == null) return List.of();
            return subCommand.getOptions();
        }
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

    public static String getRatingString(Integer rating) {
        return ":star2:".repeat(rating);
    }

    public static String getUserStringValue(User user) {
        if (user instanceof DiscordUser discordUser) return String.format("<@%s>", discordUser.getDiscordId().asString());
        if (user instanceof WebUser webUser) return String.format("`%s`", webUser.getUsername());
        return "`unknown`";
    }

    public static String getDiscordUserName(InteractionCreateEvent event) {
        return event.getInteraction().getUser().getGlobalName().orElse(null);
    }

    public static Snowflake getDiscordUserId(InteractionCreateEvent event) {
        return event.getInteraction().getUser().getId();
    }

    public static int getAmountOfPages(int items, int pageSize) {
        return (int) Math.ceil((double) items / pageSize);
    }

    public static ActionRow getPaginationComponent(int pageNumber, int amountOfPages, String commandName, String... options) {
        var optionsCombined = String.join("$", options);
        if (!optionsCombined.isEmpty()) optionsCombined = "$" + optionsCombined;

        var previousButton = Button.secondary(String.format("%s$page-previous$%d%s", commandName, pageNumber-1, optionsCombined), ReactionEmoji.unicode("⬅")).disabled(pageNumber <= 0 || amountOfPages == 0);
        var reloadButton = Button.secondary(String.format("%s$page-refresh$%d%s", commandName, pageNumber, optionsCombined), String.format("%d/%d", amountOfPages == 0 ? 0 : pageNumber+1, amountOfPages));
        var nextButton = Button.secondary(String.format("%s$page-next$%d%s", commandName, pageNumber+1, optionsCombined), ReactionEmoji.unicode("➡")).disabled(pageNumber == amountOfPages - 1 || amountOfPages == 0);

        return ActionRow.of(previousButton, reloadButton, nextButton);
    }

    private static long getEpochSeconds(LocalDateTime date) {
        return date.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(date));
    }

    public static String discordTimeAgoText(LocalDateTime date) {
        return String.format("<t:%d:R>", getEpochSeconds(date));
    }

    public static String discordFormatDate(LocalDateTime date) {
        return String.format("<t:%d:d>", getEpochSeconds(date));
    }

    public static String discordFormatTime(LocalDateTime date) {
        return String.format("<t:%d:t>", getEpochSeconds(date));
    }

    public static String discordFormatDateTime(LocalDateTime date) {
        return String.format("<t:%d:f>", getEpochSeconds(date));
    }

    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(dateFormat));
    }

    public static String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern(timeFormat));
    }

    public static String formatDateTime(LocalDateTime date) {
        return formatDate(date.toLocalDate()) + " " + formatTime(date.toLocalTime());
    }

    public static <T> PageImpl<T> getPageForList(int pageNum, int pageSize, List<T> list) {
        var pageRequest = PageRequest.of(pageNum, pageSize);
        var start = (int) pageRequest.getOffset();
        var end = Math.min((start + pageRequest.getPageSize()), list.size());
        var pageContent = list.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, list.size());
    }

    public static String addCongratulationsString(ExerciseLog log, Integer spacing, Boolean includeTitle) {
        var itemSpacing = CommandConstants.DiscordLineSpacing.repeat(spacing);
        var congratulations = new StringBuilder();
        // Add new exercise started congratulations
        if (log.isFirstLogOfExercise()) {
            congratulations.append(itemSpacing).append(":sparkles: New exercise started!\n");
        }
        // Add new pr reached congratulations
        if (log.isPrImproved()) {
            congratulations.append(itemSpacing).append(":rocket: New PR reached!\n");
        }
        // Add on pr congratulations
        if (log.isOnPr()) {
            congratulations.append(itemSpacing).append(":fire: You are on your Pr!\n");
        }
        // Add goal reached congratulations
        if (log.isGoalReached()) {
            congratulations.append(itemSpacing).append(":chart_with_upwards_trend: Goal completed!\n");
        }
        // Add new level reached congratulations
        if (log.isLevelCompleted()) {
            congratulations.append(itemSpacing).append(":star2: Level completed!\n");
        }
        if (!congratulations.isEmpty()) {
            var returnStringBuilder = new StringBuilder();
            if (includeTitle) {
                returnStringBuilder.append("Congratulations:\n");
            }
            returnStringBuilder.append(congratulations);
            return returnStringBuilder.toString();
        }
        return "";
    }

    public static String getCommandReference(String... commandNames) {
        var reference = new StringBuilder();
        for (int i = 0; i < commandNames.length; i++) {
            var commandName = commandNames[i];
            reference.append(String.format("</%s:%d> ", commandName, CommandRegistrarService.getCommandId(commandName)));
        }
        return reference.toString();
    }
}
