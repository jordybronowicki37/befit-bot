package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HabitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "habit_log_seq")
    @SequenceGenerator(name = "habit_log_seq", sequenceName = "habit_log_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    private LocalDateTime created = LocalDateTime.now();

    @NonNull
    private LocalDate logDate;

    @NonNull
    @ManyToOne
    private Habit habit;

    @NonNull
    @ManyToOne
    private User user;
}
