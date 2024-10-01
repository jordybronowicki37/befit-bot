package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@IdClass(ExerciseRecord.ExerciseRecordId.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExerciseRecord {
    @Id
    @NonNull
    @ManyToOne
    @EqualsAndHashCode.Include
    private User user;

    @Id
    @NonNull
    @ManyToOne
    @EqualsAndHashCode.Include
    private ExerciseType exerciseType;

    @NonNull
    @Setter
    private Double amount;

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ExerciseRecordId implements Serializable {
        private User user;
        private ExerciseType exerciseType;
    }
}
