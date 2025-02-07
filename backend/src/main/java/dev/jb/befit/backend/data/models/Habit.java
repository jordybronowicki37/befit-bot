package dev.jb.befit.backend.data.models;

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
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "habit_seq")
    @SequenceGenerator(name = "habit_seq", sequenceName = "habit_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    private LocalDateTime created = LocalDateTime.now();

    @NonNull
    @Setter
    @Column(length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Setter
    @NonNull
    private HabitTimeRange habitTimeRange;

    @NonNull
    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "habit")
    private List<HabitLog> habitLogs = new ArrayList<>();
}
