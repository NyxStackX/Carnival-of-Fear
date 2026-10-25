package com.epitech.fnah.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuseBoxTest {

    @Test
    void startsWorkingAndTripsOneCircuitAtATime() {
        FuseBox fuses = new FuseBox();
        assertNull(fuses.getTripped());
        assertTrue(fuses.trip(FuseBox.Circuit.CAMERAS));
        assertTrue(fuses.isTripped(FuseBox.Circuit.CAMERAS));
        assertFalse(fuses.trip(FuseBox.Circuit.LIGHTS));
        assertFalse(fuses.isTripped(FuseBox.Circuit.LIGHTS));
    }

    @Test
    void holdingTheLeverLongEnoughRestoresTheCircuit() {
        FuseBox fuses = new FuseBox();
        List<FuseBox.Circuit> seen = new ArrayList<>();
        fuses.onChange(f -> seen.add(f.getTripped()));
        fuses.trip(FuseBox.Circuit.LIGHTS);
        fuses.setResetting(true);
        fuses.update(FuseBox.RESET_TIME / 2);
        assertEquals(0.5, fuses.getProgress(), 1e-9);
        fuses.update(FuseBox.RESET_TIME / 2);
        assertNull(fuses.getTripped());
        assertFalse(fuses.isResetting());
        assertEquals(Arrays.asList(FuseBox.Circuit.LIGHTS, null), seen);
    }

    @Test
    void releasingTheLeverLosesProgress() {
        FuseBox fuses = new FuseBox();
        fuses.trip(FuseBox.Circuit.CAMERAS);
        fuses.setResetting(true);
        fuses.update(2.0);
        fuses.setResetting(false);
        fuses.update(5.0);
        assertEquals(0.0, fuses.getProgress());
        assertTrue(fuses.isTripped(FuseBox.Circuit.CAMERAS));
    }

    @Test
    void leverDoesNothingWhenNothingTripped() {
        FuseBox fuses = new FuseBox();
        fuses.setResetting(true);
        fuses.update(10);
        assertEquals(0.0, fuses.getProgress());
    }

    @Test
    void resetAndValidation() {
        FuseBox fuses = new FuseBox();
        fuses.trip(FuseBox.Circuit.CAMERAS);
        fuses.reset();
        assertNull(fuses.getTripped());
        assertThrows(NullPointerException.class, () -> fuses.trip(null));
        assertThrows(NullPointerException.class, () -> fuses.onChange(null));
        assertThrows(IllegalArgumentException.class, () -> fuses.update(-1));
    }
}
