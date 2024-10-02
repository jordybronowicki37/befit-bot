package dev.jb.befit.backend.data.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exercise_log_seq")
    @SequenceGenerator(name = "exercise_log_seq" ,sequenceName = "exercise_log_seq", allocationSize = 1)
    @Setter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Include
    private Long id;

    @CreationTimestamp
    private LocalDateTime created;

    @NonNull
    @Enumerated
    private Achievement achievement;

    @NonNull
    @ManyToOne
    private User user;
}
