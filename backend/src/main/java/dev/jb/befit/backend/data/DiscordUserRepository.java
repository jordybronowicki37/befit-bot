package dev.jb.befit.backend.data;

import dev.jb.befit.backend.data.models.DiscordUser;
import discord4j.common.util.Snowflake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordUserRepository extends JpaRepository<DiscordUser, Long> {
    Optional<DiscordUser> findByDiscordId(Snowflake discordId);
}
