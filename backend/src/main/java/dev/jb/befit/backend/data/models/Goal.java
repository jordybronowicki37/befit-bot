package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    private LocalDateTime created = LocalDateTime.now();

    @NonNull
    private Integer amount;

    @Enumerated(EnumType.STRING)
    private GoalStatus status;

    @NonNull
    @ManyToOne
    private ExerciseType exerciseType;

    @NonNull
    @ManyToOne
    private User user;
}
