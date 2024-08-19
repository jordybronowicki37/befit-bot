package dev.jb.befit.backend.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND;
import static discord4j.core.object.command.ApplicationCommandOption.Type.SUB_COMMAND_GROUP;

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
}
