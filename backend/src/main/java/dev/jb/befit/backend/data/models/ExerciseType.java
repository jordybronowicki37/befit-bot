package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExerciseType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exercise_type_seq")
    @SequenceGenerator(name = "exercise_type_seq", sequenceName = "exercise_type_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    @NonNull
    @Column(unique = true, length = 64)
    private String name;

    @OneToMany(mappedBy = "exerciseType")
    private List<ExerciseLog> exerciseLogs = new ArrayList<>();

    @NonNull
    @Enumerated(EnumType.STRING)
    private MeasurementType measurementType;

    @OneToMany(mappedBy = "exerciseType")
    private List<Goal> goals = new ArrayList<>();

    @OneToMany(mappedBy = "exerciseType")
    private List<ExerciseRecord> exerciseRecords = new ArrayList<>();

    @NonNull
    @Enumerated(EnumType.STRING)
    private GoalDirection goalDirection;
}
