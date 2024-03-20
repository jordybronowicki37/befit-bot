package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class ExerciseType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @NonNull
    @Column(unique = true, length = 64)
    private String name;

    @OneToMany(mappedBy = "exerciseType")
    private List<ExerciseLog> exerciseLogs;

    @NonNull
    @Column(length = 8)
    private String measurementType;
}
