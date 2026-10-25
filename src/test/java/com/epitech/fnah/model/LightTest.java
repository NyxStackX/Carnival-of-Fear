package com.epitech.fnah.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LightTest {

    @Test
    void startsOffAndConsumesNothing() {
        Light light = new Light(Door.Side.LEFT);
        assertFalse(light.isOn());
        assertEquals(0, light.getPowerUsage());
        assertEquals(Door.Side.LEFT, light.getSide());
    }

    @Test
    void consumesPowerWhenOn() {
        Light light = new Light(Door.Side.RIGHT);
        light.setOn(true);
        assertTrue(light.isOn());
        assertEquals(Light.POWER_USAGE_WHEN_ON, light.getPowerUsage());
    }

    @Test
    void listenersOnlyHearRealChanges() {
        Light light = new Light(Door.Side.LEFT);
        List<Boolean> seen = new ArrayList<>();
        light.onToggle(l -> seen.add(l.isOn()));
        light.setOn(true);
        light.setOn(true);
        light.setOn(false);
        light.setOn(false);
        assertEquals(List.of(true, false), seen);
    }

    @Test
    void resetSwitchesOffSilently() {
        Light light = new Light(Door.Side.LEFT);
        List<Boolean> seen = new ArrayList<>();
        light.setOn(true);
        light.onToggle(l -> seen.add(l.isOn()));
        light.reset();
        assertFalse(light.isOn());
        assertTrue(seen.isEmpty());
    }

    @Test
    void rejectsMissingArguments() {
        assertThrows(NullPointerException.class, () -> new Light(null));
        assertThrows(NullPointerException.class, () -> new Light(Door.Side.LEFT).onToggle(null));
    }
}
