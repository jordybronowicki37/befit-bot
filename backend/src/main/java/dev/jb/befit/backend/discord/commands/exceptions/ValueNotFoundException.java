package dev.jb.befit.backend.discord.commands.exceptions;

import dev.jb.befit.backend.service.exceptions.MyException;

public class ValueNotFoundException extends MyException {
    public ValueNotFoundException(String optionName) {
        super(String.format("Value not found for option: %s", optionName));
    }
}
