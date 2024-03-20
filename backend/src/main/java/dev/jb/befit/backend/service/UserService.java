package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.DiscordUserRepository;
import dev.jb.befit.backend.data.models.DiscordUser;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final DiscordUserRepository discordUserRepository;

    public DiscordUser getOrCreateDiscordUser(Snowflake userId) {
        var discordUser = discordUserRepository.findByDiscordId(userId);
        if (discordUser.isEmpty()) {
            var newDiscordUser = new DiscordUser(userId);
            return discordUserRepository.save(newDiscordUser);
        }
        return discordUser.get();
    }
}
