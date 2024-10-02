package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.data.models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementsRepository extends JpaRepository<UserAchievement, Long> {
    long countDistinctByAchievement(Achievement achievement);
    List<UserAchievement> findAllByAchievement(Achievement achievement);
    List<UserAchievement> findAllByUser(User user);
    Optional<UserAchievement> findByUserIdAndAchievement(Long userId, Achievement achievement);
}
