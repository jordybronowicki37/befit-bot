package dev.jb.befit.backend.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class WebUser extends User {
    @NonNull
    @Column(unique = true)
    private String username;

    @NonNull
    @Column(unique = true)
    private String email;

    @NonNull
    private String password;
}
