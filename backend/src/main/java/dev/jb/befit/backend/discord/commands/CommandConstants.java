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
    public static final String CommandGoalsView = "goals view";
    public static final String CommandGoalsCancel = "goals cancel";
    public static final String CommandHabitsAdd = "habits add";
    public static final String CommandHabitsCheck = "habits check";
    public static final String CommandHabitsViewAll = "habits view all";
    public static final String CommandHabitsViewOne = "habits view one";
    public static final String CommandHabitsRemove = "habits remove";
    public static final String CommandHabitsProgress = "habits progress";
    public static final String CommandHelp = "help";
    public static final String CommandHistory = "history";
    public static final String CommandLeaderboard = "leaderboard";
    public static final String CommandLog = "log";
    public static final String CommandLogUndo = "log undo";
    public static final String CommandManagementRestart = "management restart";
    public static final String CommandManagementRefreshCommands = "management refresh-commands";
    public static final String CommandManagementJobsAdd = "management jobs add";
    public static final String CommandManagementJobsRemove = "management jobs remove";
    public static final String CommandMotivation = "motivation";
    public static final String CommandProgress = "progress";
    public static final String CommandSessionsCreate = "sessions create";
    public static final String CommandSessionsRate = "sessions rate";
    public static final String CommandSessionsViewAll = "sessions view all";
    public static final String CommandSessionsViewOne = "sessions view one";
    public static final String CommandSessionsViewLast = "sessions view last";
    public static final String CommandSessionsStop = "sessions stop";
    public static final String CommandStats = "stats";

    public static final String AutoCompletePropExerciseName = "exercise-name";
    public static final String AutoCompletePropMyExerciseName = "my-exercise-name";
    public static final String AutoCompletePropGoal = "goal";
    public static final String AutoCompletePropHabit = "habit";
    public static final String AutoCompletePropSession = "session";
    public static final String AutoCompletePropSessionActive = "active-session";
    public static final String AutoCompletePropScheduledJob = "scheduled-job";

    public static final Integer PageSize = 5;
    public static final Integer PageSizeSmallItems = 10;
    public static final Integer SearchResultsSize = 25;
}
