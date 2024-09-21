package dev.jb.befit.backend.data.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduledJobType {
    GYM_REMINDER("Gym Reminder"),
    MOTIVATIONAL_MESSAGE("Motivational Message");

    private final String displayName;
}
