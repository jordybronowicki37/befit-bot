package dev.jb.befit.backend.discord.commands.handlers.help;

import java.util.List;

public record HelpResource(
        List<HelpCommandResource> commands
) {
}
