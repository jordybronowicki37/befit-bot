package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Setter
    @OneToOne
    private Goal reachedGoal;

    @NonNull
    @ManyToOne
    private User user;
}
