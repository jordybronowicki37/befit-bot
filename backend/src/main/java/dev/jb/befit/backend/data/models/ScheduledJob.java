package dev.jb.befit.backend.data.models;

import dev.jb.befit.backend.data.converters.SnowflakeConverter;
import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZoneId;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScheduledJob {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduled_job_seq")
    @SequenceGenerator(name = "scheduled_job_seq" ,sequenceName = "scheduled_job_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    private ScheduledJobType type;

    @NonNull
    @Convert(converter = SnowflakeConverter.class)
    private Snowflake channelId;

    @NonNull
    private String cronExpression;

    @NonNull
    private ZoneId timeZone;
}
