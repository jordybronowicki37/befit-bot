package dev.jb.befit.backend.data.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Achievement {
    BODYBUILDER(AchievementDifficulty.UNKNOWN, "icon-01.png", "ach_bodybuilder", "Bodybuilder", "WIP"),
    LIKE_A_MARATHON(AchievementDifficulty.IMPOSSIBLE, "icon-02.png", "ach_like_a_marathon", "Like a marathon", "Reach a distance on any exercise of 42km."),
    HEART_MONITOR(AchievementDifficulty.EASY, "icon-03.png", "ach_heart_monitor", "Heart monitor", "Log an exercise which uses bpm as a measurement."),
    ON_A_ROLL(AchievementDifficulty.MEDIUM, "icon-04.png", "ach_on_a_roll", "On a roll", "Log an exercise 4 days in a row."),
    THINK_OF_HEALTH(AchievementDifficulty.MEDIUM, "icon-05.png", "ach_think_about_health", "Think about your health", "Log an exercise that burns 200 calories."),
    THE_HULK(AchievementDifficulty.HARD, "icon-06.png", "ach_hulk", "The hulk", "Lift something weighing more than 100kg."),
    FULL_WORKOUT(AchievementDifficulty.MEDIUM, "icon-07.png", "ach_full_workout", "Full workout", "Within 24h log an exercise for the following categories: weight, time and distance based."),
    DONE_FOR_TODAY(AchievementDifficulty.MEDIUM, "icon-08.png", "ach_done_for_today", "Done for today", "Create 10 logs on a single day."),
    KEEP_ON_STACKING(AchievementDifficulty.MEDIUM, "icon-09.png", "ach_keep_on_stacking", "Keep on stacking", "Have 5 concurrent logs of a single exercise that keep increasing."),
    SERIOUS_DEDICATION(AchievementDifficulty.IMPOSSIBLE, "icon-10.png", "ach_serious_dedication", "Serious dedication", "Create logs each day for an entire month."),
    ON_THE_BENCH(AchievementDifficulty.UNKNOWN, "icon-11.png", "ach_on_the_bench", "On the bench", "WIP"),
    GOAL_REACHED(AchievementDifficulty.EASY, "icon-12.png", "ach_goal_reached", "Reach your potential", "Complete a goal."),
    LETS_GO_PLACES(AchievementDifficulty.HARD, "icon-13.png", "ach_lets_go_places", "Lets go places", "Reach a distance on any exercise of 20km."),
    SHOW_OFF(AchievementDifficulty.HARD, "icon-14.png", "ach_show_off", "Show off", "Reach the first place on an exercise leaderboard that has at least 6 participants."),
    THE_GOAT(AchievementDifficulty.HARD, "icon-15.png", "ach_the_goat", "The goat", "Create a total of 100 logs."),
    THE_RIGHT_MINDSET(AchievementDifficulty.MEDIUM, "icon-16.png", "ach_the_right_mindset", "The right mindset", "Set 5 goals and complete these within a month."),
    HEALTHY(AchievementDifficulty.EASY, "icon-17.png", "ach_healthy", "Lets get healthy", "Create your first log."),
    HOME_GYM(AchievementDifficulty.HARD, "icon-18.png", "ach_home_gym", "Feels like home", "Log an exercise 10 days in a row."),
    CARDIO_ENTHUSIAST(AchievementDifficulty.MEDIUM, "icon-19.png", "ach_cardio_enthusiast", "Cardio enthusiast", "Do any exercise for 30 minutes."),
    LOVE_TO_LIFT(AchievementDifficulty.MEDIUM, "icon-20.png", "ach_love_lifting", "Love to lift", "Lift something weighing more than 50kg for 3 days in a row.");

    private final AchievementDifficulty difficulty;
    private final String iconFileName;
    private final String displayName;
    private final String title;
    private final String description;
}
