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
                Arguments.of(240, 3, 250, 20, 220, 490),
                Arguments.of(600, 4, 480, 110, 490, 1080),
                Arguments.of(2000, 5, 380, 920, 1080, 2380)
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
        var experienceLevelDetails = UserService.getLevelData(xp);

        System.out.println(experienceLevelDetails);

        // Assertions
        assertEquals(expectedLevel, experienceLevelDetails.level());
        assertEquals(expectedRemainingXp, experienceLevelDetails.xpRemainingInLevel());
        assertEquals(expectedCompletedXp, experienceLevelDetails.xpCompletedInLevel());
        assertEquals(expectedBottomLevelXp, experienceLevelDetails.xpBottomLevel());
        assertEquals(expectedTopLevelXp, experienceLevelDetails.xpTopLevel());
    }
}