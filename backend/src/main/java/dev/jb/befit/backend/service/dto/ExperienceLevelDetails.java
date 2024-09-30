package dev.jb.befit.backend.service.dto;

public record ExperienceLevelDetails(
        Long level,
        Long xpRemainingInLevel,
        Long xpCompletedInLevel,
        Long xpBottomLevel,
        Long xpTopLevel
) {}
