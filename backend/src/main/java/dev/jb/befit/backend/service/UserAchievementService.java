package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.UserAchievementsRepository;
import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.data.models.UserAchievement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAchievementService {
    private final UserService userService;
    private final UserExperienceService userExperienceService;
    private final UserAchievementsRepository achievementsRepository;

    public List<UserAchievement> getAllOfAchievement(Achievement achievement) {
        return achievementsRepository.findAllByAchievement(achievement);
    }

    public List<UserAchievement> getAllOfUser(User user) {
        return achievementsRepository.findAllByUser(user);
    }

    public Optional<UserAchievement> hasCompletedAchievement(Long userId, Achievement achievement) {
        return achievementsRepository.findByUserIdAndAchievement(userId, achievement);
    }

    public Optional<UserAchievement> hasCompletedAchievement(User user, Achievement achievement) {
        return hasCompletedAchievement(user.getId(), achievement);
    }

    public UserAchievement completeAchievement(User user, Achievement achievement) {
        var foundAchievement = hasCompletedAchievement(user, achievement);
        if (foundAchievement.isPresent()) return foundAchievement.get();

        var earnedXp = switch (achievement.getDifficulty()) {
            case EASY -> 50;
            case MEDIUM -> 100;
            case HARD -> 150;
            case IMPOSSIBLE -> 200;
            default -> 0;
        };
        if (earnedXp != 0) userExperienceService.addExperience(user, earnedXp);

        var userAchievement = new UserAchievement(achievement, user);
        return achievementsRepository.save(userAchievement);
    }

    public double getCompletionPercentage(Achievement achievement) {
        var amountOfUsers = userService.getAmountOfUsers();
        var amountOfCompletedAchievements = achievementsRepository.countDistinctByAchievement(achievement);
        if (amountOfCompletedAchievements == 0 | amountOfUsers == 0) return 0;
        return (double) amountOfCompletedAchievements / amountOfUsers * 100;
    }
}
