package dev.jb.befit.backend.discord.commands.exceptions;

import dev.jb.befit.backend.service.exceptions.MyException;

public class OptionNotFoundException extends MyException {
    public OptionNotFoundException(String optionName) {
        super(String.format("Option %s not found", optionName));
    }
}
