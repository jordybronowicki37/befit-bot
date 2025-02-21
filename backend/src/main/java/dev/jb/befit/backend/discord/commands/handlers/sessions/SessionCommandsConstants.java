package dev.jb.befit.backend.discord.commands.handlers.sessions;

import dev.jb.befit.backend.discord.commands.CommandConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionCommandsConstants {
    public static final String sessionCreateEmbedTitle = ":notepad_spiral: New session created";
    public static final String sessionStopEmbedTitle = ":notepad_spiral: Session stopped";
    public static final String sessionViewEmbedTitle = ":notepad_spiral: Session";
    public static final List<String> sessionSingleCommands = List.of(
            CommandConstants.CommandSessionsViewOne,
            CommandConstants.CommandSessionsViewLast,
            CommandConstants.CommandSessionsCreate,
            CommandConstants.CommandSessionsStop
    );
}
