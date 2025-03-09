package dev.jb.befit.backend.discord.commands.handlers.help;

import java.util.List;

public record HelpCommandResource(
        String command,
        String description,
        String image,
        List<HelpCommandArgumentResource> arguments
) {
    public record HelpCommandArgumentResource(
            String name,
            String description,
            Boolean required,
            Boolean autocomplete,
            Boolean choices
    ) {}
}
