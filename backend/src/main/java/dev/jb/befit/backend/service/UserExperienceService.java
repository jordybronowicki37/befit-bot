package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.UserRepository;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.dto.ExperienceLevelDetails;
import dev.jb.befit.backend.service.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserExperienceService {
    private final UserRepository userRepository;

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
        var topLevelXp = ServiceConstants.XpStartingLimit;
        var level = 1L;

        while (xp >= topLevelXp) {
            level++;
            bottomLevelXp = topLevelXp;
            topLevelXp += (long) (Math.ceil(topLevelXp * ServiceConstants.XpGrowthRate / 10) * 10);
        }

        var remainingLevelXp = topLevelXp - xp;
        var completedLevelXp = xp - bottomLevelXp;

        return new ExperienceLevelDetails(level, remainingLevelXp, completedLevelXp, bottomLevelXp, topLevelXp);
    }
}
