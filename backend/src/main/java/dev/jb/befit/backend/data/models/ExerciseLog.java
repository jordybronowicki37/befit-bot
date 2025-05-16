package dev.jb.befit.backend.data.models;

import dev.jb.befit.backend.data.converters.SnowflakeConverter;
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
public class ExerciseLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exercise_log_seq")
    @SequenceGenerator(name = "exercise_log_seq" ,sequenceName = "exercise_log_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    @Setter
    private LocalDateTime created = LocalDateTime.now();

    @NonNull
    private Double amount;

    @NonNull
    @ManyToOne
    private ExerciseType exerciseType;

    @NonNull
    @ManyToOne
    private User user;

    @Setter
    @OneToOne
    private Goal reachedGoal;

    @Setter
    @ManyToOne
    private ExerciseSession session;

    @Setter
    @Convert(converter = SnowflakeConverter.class)
    private Snowflake discordChannelId;

    @Setter
    @Convert(converter = SnowflakeConverter.class)
    private Snowflake discordMessageId;

    @OneToMany(mappedBy = "log", fetch = FetchType.EAGER)
    private List<UserAchievement> achievements = new ArrayList<>();

    @Setter
    @Column(columnDefinition="BIGINT DEFAULT 0", nullable = false)
    private Long earnedXp = 0L;

    @Setter
    @Column(columnDefinition="BOOLEAN DEFAULT FALSE")
    private boolean prImproved = false;

    @Setter
    @Column(columnDefinition="BOOLEAN DEFAULT FALSE")
    private boolean onPr = false;

    @Setter
    @Column(columnDefinition="BOOLEAN DEFAULT FALSE")
    private boolean levelCompleted = false;

    @Setter
    @Column(columnDefinition="BOOLEAN DEFAULT FALSE")
    private boolean firstLogOfExercise = false;

    public boolean isGoalReached() {
        return reachedGoal != null;
    }
}
