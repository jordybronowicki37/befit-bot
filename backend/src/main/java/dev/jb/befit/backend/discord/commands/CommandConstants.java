package dev.jb.befit.backend.discord.commands;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandConstants {
    public static final String CommandAchievements = "achievements";
    public static final String CommandExercisesViewAll = "exercises view all";
    public static final String CommandExercisesViewOne = "exercises view one";
    public static final String CommandExercisesViewMy = "exercises view my";
    public static final String CommandExercisesCreate = "exercises create";
    public static final String CommandGoalsAdd = "goals add";
    public static final String CommandLog = "log";
    public static final String CommandManagementRestart = "management restart";
    public static final String CommandManagementRefreshCommands = "management refresh-commands";
    public static final String CommandManagementJobsAdd = "management jobs add";
    public static final String CommandManagementJobsRemove = "management jobs remove";
    public static final String CommandMotivation = "motivation";
    public static final String CommandProgress = "progress";

    public static final String AutoCompletePropExerciseName = "exercise-name";
    public static final String AutoCompletePropScheduledJob = "scheduled-job";

    public static final Integer PageSize = 5;
    public static final Integer SearchResultsSize = 25;
}
