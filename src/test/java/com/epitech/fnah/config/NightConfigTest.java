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
}
