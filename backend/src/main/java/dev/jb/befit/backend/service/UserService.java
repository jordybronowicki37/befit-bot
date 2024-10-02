package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.DiscordUserRepository;
import dev.jb.befit.backend.data.UserRepository;
import dev.jb.befit.backend.data.models.DiscordUser;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.dto.ExperienceLevelDetails;
import dev.jb.befit.backend.service.exceptions.UserNotFoundException;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final double GROWTH_RATE = 1.3;
    private static final long STARTING_LEVEL_LIMIT = 200L;

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

    public void addExperience(long userId, long experience) {
        if (experience < 0) throw new IllegalArgumentException("Experience cannot be negative");
        var user = userRepository.findById(userId);
        if (user.isEmpty()) throw new UserNotFoundException(userId);
        addExperience(user.get(), experience);
    }

    public void addExperience(User user, long experience) {
        if (experience < 0) throw new IllegalArgumentException("Experience cannot be negative");
        user.setXp(user.getXp() + experience);
        userRepository.save(user);
    }

    public static ExperienceLevelDetails getLevelData(long xp) {
        var bottomLevelXp = 0L;
        var topLevelXp = STARTING_LEVEL_LIMIT;
        var level = 1L;

        while (xp >= topLevelXp) {
            level++;
            bottomLevelXp = topLevelXp;
            topLevelXp += (long) (Math.ceil(topLevelXp * GROWTH_RATE / 10) * 10);
        }

        var remainingLevelXp = topLevelXp - xp;
        var completedLevelXp = xp - bottomLevelXp;

        return new ExperienceLevelDetails(level, remainingLevelXp, completedLevelXp, bottomLevelXp, topLevelXp);
    }
}
