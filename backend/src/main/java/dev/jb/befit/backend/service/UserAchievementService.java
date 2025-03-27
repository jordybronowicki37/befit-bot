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
    private final UserAchievementsRepository achievementsRepository;

    public List<UserAchievement> getAllOfAchievement(Achievement achievement) {
        return achievementsRepository.findAllByAchievement(achievement);
    }

    public List<UserAchievement> getAllOfUser(User user) {
        return achievementsRepository.findAllByUser(user);
    }

    public Optional<UserAchievement> hasCompletedAchievement(User user, Achievement achievement) {
        return achievementsRepository.findByUserAndAchievement(user, achievement);
    }

    public UserAchievement completeAchievement(User user, Achievement achievement) {
        var foundAchievement = hasCompletedAchievement(user, achievement);
        if (foundAchievement.isPresent()) return foundAchievement.get();

        var userAchievement = new UserAchievement(achievement, user);
        return achievementsRepository.save(userAchievement);
    }

    public double getCompletionPercentage(Achievement achievement) {
        var amountOfUsers = userService.getAmountOfUsers();
        var amountOfCompletedAchievements = achievementsRepository.countDistinctByAchievement(achievement);
        if (amountOfCompletedAchievements == 0 | amountOfUsers == 0) return 0;
        return (double) amountOfCompletedAchievements / amountOfUsers * 100;
    }

    public static long getEarnedAchievementXp(Achievement achievement) {
        return switch (achievement.getDifficulty()) {
            case EASY -> ServiceConstants.EarnedXpAchievementCompletedEasy;
            case MEDIUM -> ServiceConstants.EarnedXpAchievementCompletedMedium;
            case HARD -> ServiceConstants.EarnedXpAchievementCompletedHard;
            case IMPOSSIBLE -> ServiceConstants.EarnedXpAchievementCompletedImpossible;
            default -> 0;
        };
    }
}
