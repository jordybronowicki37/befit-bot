package dev.jb.befit.backend.data.models;

import dev.jb.befit.backend.data.converters.SnowflakeConverter;
import dev.jb.befit.backend.service.ServiceConstants;
import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExerciseSession {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_seq")
    @SequenceGenerator(name = "session_seq" ,sequenceName = "session_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    @Setter
    private LocalDateTime created = LocalDateTime.now();

    @Setter
    private LocalDateTime ended = LocalDateTime.now().plusSeconds(ServiceConstants.SessionTimeout.getEpochSecond());

    @NonNull
    @Setter
    private String name;

    @Setter
    @Enumerated
    private ExerciseSessionStatus status = ExerciseSessionStatus.ACTIVE;

    @OneToMany(mappedBy = "session")
    private List<ExerciseLog> exerciseLogs = new ArrayList<>();

    @NonNull
    @ManyToOne
    private User user;

    @Setter
    @Convert(converter = SnowflakeConverter.class)
    private Snowflake discordChannelId;

    @Setter
    private Integer rating = null;
}
