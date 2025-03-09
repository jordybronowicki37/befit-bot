package dev.jb.befit.backend.discord.commands.exceptions;

import dev.jb.befit.backend.service.exceptions.MyException;

public class CommandNotFoundException extends MyException {
    public CommandNotFoundException() {
        super("The command was not found");
    }
}
