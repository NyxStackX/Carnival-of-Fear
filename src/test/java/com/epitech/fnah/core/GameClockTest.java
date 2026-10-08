package com.epitech.fnah.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameClockTest {

    private GameClock clock;

    @BeforeEach
    void setUp() {
        clock = new GameClock();
    }

    @Test
    void startsAtMidnight() {
        assertEquals(0, clock.getHour());
        assertEquals(0, clock.getMinute());
        assertEquals("00:00", clock.getFormattedTime());
    }

    @Test
    void oneHourEqualsSixtySeconds() {
        clock.update(60.0);
        assertEquals(1, clock.getHour());
        assertEquals("01:00", clock.getFormattedTime());
    }

    @Test
    void halfHourIsThirtySeconds() {
        clock.update(30.0);
        assertEquals(0, clock.getHour());
        assertEquals(30, clock.getMinute());
    }

    @Test
    void reachesSixAMAfterSixMinutes() {
        clock.update(360.0);
        assertTrue(clock.isNightOver());
        assertEquals("06:00", clock.getFormattedTime());
    }

    @Test
    void doesNotGoPastSixAM() {
        clock.update(1000.0);
        assertEquals(GameClock.NIGHT_END_HOUR, clock.getHour());
        assertTrue(clock.isNightOver());
    }

    @Test
    void rejectsNegativeDelta() {
        assertThrows(IllegalArgumentException.class, () -> clock.update(-1.0));
    }

    @Test
    void firesHourListenersInOrder() {
        List<Integer> fired = new ArrayList<>();
        clock.onHourChange(fired::add);
        clock.update(360.0); // full night
        assertEquals(List.of(1, 2, 3, 4, 5, 6), fired);
    }

    @Test
    void doesNotFireListenerForHourZero() {
        List<Integer> fired = new ArrayList<>();
        clock.onHourChange(fired::add);
        clock.update(10.0);
        assertTrue(fired.isEmpty());
    }

    @Test
    void resetBringsBackToMidnight() {
        clock.update(120.0);
        clock.reset();
        assertEquals(0, clock.getHour());
        assertEquals(0.0, clock.getElapsedSeconds());
    }
}