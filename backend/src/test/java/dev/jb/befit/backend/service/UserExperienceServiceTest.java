package dev.jb.befit.backend.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserExperienceServiceTest {

    static Stream<Arguments> levelTestData() {
        return Stream.of(
                Arguments.of(90, 1, 10, 90, 0, 100),
                Arguments.of(130, 2, 90, 30, 100, 220),
                Arguments.of(240, 3, 230, 20, 220, 470),
                Arguments.of(600, 4, 390, 130, 470, 990),
                Arguments.of(2000, 5, 80, 1010, 990, 2080)
        );
    }

    @ParameterizedTest
    @MethodSource("levelTestData")
    void getLevelData(long xp,
                      long expectedLevel,
                      long expectedRemainingXp,
                      long expectedCompletedXp,
                      long expectedBottomLevelXp,
                      long expectedTopLevelXp
    ) {
        var experienceLevelDetails = UserExperienceService.getLevelData(xp);

        System.out.println(experienceLevelDetails);

        // Assertions
        assertEquals(expectedLevel, experienceLevelDetails.level());
        assertEquals(expectedRemainingXp, experienceLevelDetails.xpRemainingInLevel());
        assertEquals(expectedCompletedXp, experienceLevelDetails.xpCompletedInLevel());
        assertEquals(expectedBottomLevelXp, experienceLevelDetails.xpBottomLevel());
        assertEquals(expectedTopLevelXp, experienceLevelDetails.xpTopLevel());
    }
}