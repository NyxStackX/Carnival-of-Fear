package com.epitech.fnah.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomNightTest {

    @Test
    void standardPutsEveryClownAtFive() {
        CustomNight night = CustomNight.standard();
        for (int i = 0; i < CustomNight.CLOWN_COUNT; i++) {
            assertEquals(5, night.level(i));
        }
        assertFalse(night.isMaxChallenge());
    }

    @Test
    void maxChallengeIsTwentyEverywhere() {
        CustomNight night = CustomNight.maxChallenge();
        assertTrue(night.isMaxChallenge());
        assertEquals(new CustomNight(20, 20, 20, 20), night);
    }

    @Test
    void levelsAreReadByIndex() {
        CustomNight night = new CustomNight(1, 2, 3, 4);
        assertEquals(1, night.level(0));
        assertEquals(2, night.level(1));
        assertEquals(3, night.level(2));
        assertEquals(4, night.level(3));
        assertThrows(IllegalArgumentException.class, () -> night.level(4));
    }

    @Test
    void adjustChangesOnlyOneClownAndWraps() {
        CustomNight night = new CustomNight(0, 20, 5, 5);
        assertEquals(new CustomNight(20, 20, 5, 5), night.adjust(0, -1));
        assertEquals(new CustomNight(0, 0, 5, 5), night.adjust(1, 1));
        assertEquals(new CustomNight(0, 20, 7, 5), night.adjust(2, 2));
        assertThrows(IllegalArgumentException.class, () -> night.adjust(9, 1));
    }

    @Test
    void rejectsLevelsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new CustomNight(-1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new CustomNight(0, 0, 0, 21));
    }
}
