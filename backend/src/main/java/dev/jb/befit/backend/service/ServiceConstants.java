package dev.jb.befit.backend.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceConstants {
    public static final double XpGrowthRate = 1.3;
    public static final long XpStartingLimit = 100;

    public static final long EarnedXpLogCreated = 10;
    public static final long EarnedXpLogAddedToSession = 5;
    public static final long EarnedXpNewExerciseStarted = 20;
    public static final long EarnedXpRecordImproved = 50;
    public static final long EarnedXpGoalCreated = 10;
    public static final long EarnedXpGoalCompleted = 50;
    public static final long EarnedXpAchievementCompletedEasy = 50;
    public static final long EarnedXpAchievementCompletedMedium = 150;
    public static final long EarnedXpAchievementCompletedHard = 450;
    public static final long EarnedXpAchievementCompletedImpossible = 1350;
}
