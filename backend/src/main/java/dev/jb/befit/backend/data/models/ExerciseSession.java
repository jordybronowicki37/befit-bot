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
public class ExerciseSession {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_seq")
    @SequenceGenerator(name = "session_seq" ,sequenceName = "session_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    @Setter
    private LocalDateTime created = LocalDateTime.now();

    @NonNull
    @Setter
    private String name;

    @OneToMany
    private List<ExerciseLog> exerciseLogs = new ArrayList<>();

    @NonNull
    @ManyToOne
    private User user;
}
