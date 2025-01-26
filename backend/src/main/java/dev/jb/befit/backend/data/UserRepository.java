package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.User;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByOrderByXpDesc(@NonNull Pageable pageable);
    @Query(value = "SELECT row_num FROM ( SELECT id, xp, ROW_NUMBER() OVER (ORDER BY xp DESC) AS row_num FROM users ) AS leaderboard WHERE leaderboard.id = :userId", nativeQuery = true)
    Long findUserRankById(@NonNull @Param("userId") Long userId);
}
