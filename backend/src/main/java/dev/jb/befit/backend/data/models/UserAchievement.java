package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_achievement_seq")
    @SequenceGenerator(name = "user_achievement_seq" ,sequenceName = "user_achievement_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    private LocalDateTime created = LocalDateTime.now();

    @NonNull
    @Enumerated
    private Achievement achievement;

    @NonNull
    @ManyToOne
    private User user;

    @Setter
    @ManyToOne
    private ExerciseLog log;
}
