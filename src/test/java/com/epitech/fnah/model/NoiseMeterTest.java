package com.epitech.fnah.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoiseMeterTest {

    @Test
    void startsSilent() {
        NoiseMeter noise = new NoiseMeter();
        assertEquals(0.0, noise.getLevel());
        assertFalse(noise.isLoud());
        assertEquals(0, noise.hearingBonus());
    }

    @Test
    void addsUpToMaxAndGetsLoud() {
        NoiseMeter noise = new NoiseMeter();
        noise.make(60);
        assertTrue(noise.isLoud());
        assertEquals(2, noise.hearingBonus());
        noise.make(500);
        assertEquals(NoiseMeter.MAX, noise.getLevel());
        assertEquals(4, noise.hearingBonus());
    }

    @Test
    void fadesOverTime() {
        NoiseMeter noise = new NoiseMeter();
        noise.make(30);
        noise.update(1.0);
        assertEquals(30 - NoiseMeter.DECAY_PER_SECOND, noise.getLevel(), 1e-9);
        noise.update(100);
        assertEquals(0.0, noise.getLevel());
    }

    @Test
    void resetAndValidation() {
        NoiseMeter noise = new NoiseMeter();
        noise.make(40);
        noise.reset();
        assertEquals(0.0, noise.getLevel());
        assertThrows(IllegalArgumentException.class, () -> noise.make(-1));
        assertThrows(IllegalArgumentException.class, () -> noise.update(-1));
    }
}
