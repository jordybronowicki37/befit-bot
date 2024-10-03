package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.DiscordUserRepository;
import dev.jb.befit.backend.data.UserRepository;
import dev.jb.befit.backend.data.models.DiscordUser;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final DiscordUserRepository discordUserRepository;
    private final UserRepository userRepository;

    public DiscordUser getOrCreateDiscordUser(Snowflake userId) {
        var discordUser = discordUserRepository.findByDiscordId(userId);
        if (discordUser.isEmpty()) {
            var newDiscordUser = new DiscordUser(userId);
            return discordUserRepository.save(newDiscordUser);
        }
        return discordUser.get();
    }

    public long getAmountOfUsers() {
        return userRepository.count();
    }
}
