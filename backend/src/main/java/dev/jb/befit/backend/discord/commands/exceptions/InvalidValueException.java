package dev.jb.befit.backend.discord.commands.exceptions;

import dev.jb.befit.backend.service.exceptions.MyException;

public class InvalidValueException extends MyException {
    public InvalidValueException(String optionName, String value) {
        super(String.format("Value was invalid for option: %s, value: %s", optionName, value));
    }
}
