package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.Achievement;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.data.models.UserAchievement;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementsRepository extends JpaRepository<UserAchievement, Long> {
    long countDistinctByAchievement(@NonNull Achievement achievement);
    List<UserAchievement> findAllByAchievement(@NonNull Achievement achievement);
    List<UserAchievement> findAllByUser(@NonNull User user);
    Optional<UserAchievement> findByUserIdAndAchievement(@NonNull Long userId, @NonNull Achievement achievement);
}
