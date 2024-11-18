package dev.jb.befit.backend.data.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExerciseSessionStatus {
    ACTIVE("active"),
    STOPPED("manually stopped"),
    FINISHED("finished");

    private final String displayName;
}
