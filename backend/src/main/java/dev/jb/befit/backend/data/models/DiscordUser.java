package dev.jb.befit.backend.data.models;

import dev.jb.befit.backend.data.converters.SnowflakeConverter;
import discord4j.common.util.Snowflake;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class DiscordUser extends User {
    @NonNull
    @Convert(converter = SnowflakeConverter.class)
    private Snowflake discordId;
}
