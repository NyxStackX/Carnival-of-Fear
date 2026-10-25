package com.epitech.fnah.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NightConfigTest {

    @Test
    void firstNightMatchesGdd() {
        NightConfig night = NightConfig.forNight(1);
        assertEquals(1, night.number());
        assertEquals(3, night.boboAiLevel());
        assertEquals(1.0, night.energyMultiplier());
    }

    @Test
    void nightsGetHarder() {
        for (int n = 2; n <= NightConfig.LAST_NIGHT; n++) {
            assertTrue(NightConfig.forNight(n).boboAiLevel() > NightConfig.forNight(n - 1).boboAiLevel());
        }
    }

    @Test
    void rejectsUnknownNights() {
        assertThrows(IllegalArgumentException.class, () -> NightConfig.forNight(0));
        assertThrows(IllegalArgumentException.class, () -> NightConfig.forNight(NightConfig.LAST_NIGHT + 1));
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new NightConfig(0, 3, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new NightConfig(1, 3, 0.0));
    }

    @Test
    void newCharactersGetProgressivelyHigherAiLevels() {
        assertEquals(1, NightConfig.forNight(1).mimiAiLevel());
        assertEquals(1, NightConfig.forNight(1).zigEtZagAiLevel());
        assertEquals(3, NightConfig.forNight(2).mimiAiLevel());
        assertEquals(3, NightConfig.forNight(2).zigEtZagAiLevel());
        assertEquals(12, NightConfig.forNight(5).mimiAiLevel());
        assertEquals(13, NightConfig.forNight(5).zigEtZagAiLevel());
    }
}
