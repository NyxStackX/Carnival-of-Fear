package com.epitech.fnah.core;

import com.epitech.fnah.model.FuseBox;
import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerrorDirectorTest {

    private Office office;
    private PowerSystem power;
    private GameClock clock;
    private GameContext context;
    private List<GameEvent.ScareEvent> scares;

    @BeforeEach
    void setUp() {
        office = new Office();
        power = new PowerSystem();
        clock = new GameClock();
        scares = new ArrayList<>();
        context = new GameContext(clock, power, office, MapGraph.circus(), name -> { },
                e -> {
                    if (e instanceof GameEvent.ScareEvent scare) {
                        scares.add(scare);
                    }
                });
    }

    private void run(TerrorDirector director, double seconds) {
        for (double t = 0; t < seconds; t += 0.5) {
            director.update(0.5, context);
        }
    }

    @Test
    void firstEventComesAfterACalmStart() {
        TerrorDirector director = new TerrorDirector(() -> 20);
        director.reset();
        run(director, director.intervalAt(0) * 0.5 - 1);
        assertTrue(scares.isEmpty());
        run(director, 2);
        assertEquals(1, scares.size());
    }

    @Test
    void laterNightsAndHoursAreMoreIntense() {
        TerrorDirector director = new TerrorDirector(() -> 10);
        double nightOne = director.intervalAt(0);
        director.setNight(5);
        assertTrue(director.intervalAt(0) < nightOne);
        assertTrue(director.intervalAt(5) < director.intervalAt(0));
        assertTrue(director.intervalAt(6) >= TerrorDirector.MIN_INTERVAL);
        assertThrows(IllegalArgumentException.class, () -> director.setNight(0));
    }

    @Test
    void neverRepeatsTheSameScareTwiceInARow() {
        TerrorDirector director = new TerrorDirector(() -> 3);
        director.setNight(5);
        director.reset();
        run(director, 600);
        assertTrue(scares.size() > 10);
        for (int i = 1; i < scares.size(); i++) {
            assertNotEquals(scares.get(i - 1).scare(), scares.get(i).scare());
        }
    }

    @Test
    void firstNightOnlyUsesGentleScares() {
        int[] roll = {0};
        TerrorDirector director = new TerrorDirector(() -> 1 + (roll[0]++ % 20));
        director.reset();
        run(director, 900);
        assertFalse(scares.isEmpty());
        for (GameEvent.ScareEvent scare : scares) {
            assertEquals(1, scare.scare().getFirstNight());
            assertTrue(scare.pan() >= -1 && scare.pan() <= 1);
        }
    }

    @Test
    void onlySoundsInTheDark() {
        int[] roll = {0};
        TerrorDirector director = new TerrorDirector(() -> 1 + (roll[0]++ % 20));
        director.setNight(5);
        director.reset();
        power.update(10_000);
        run(director, 600);
        assertFalse(scares.isEmpty());
        for (GameEvent.ScareEvent scare : scares) {
            assertTrue(scare.scare().isAudibleInDark());
        }
    }

    @Test
    void powerSurgeTripsAFuse() {
        int[] roll = {0};
        TerrorDirector director = new TerrorDirector(() -> 1 + (roll[0]++ % 20));
        director.setNight(5);
        director.reset();
        for (int i = 0; i < 2000 && office.getFuseBox().getTripped() == null; i++) {
            director.update(0.5, context);
        }
        assertNotNull(office.getFuseBox().getTripped());
        assertTrue(scares.stream().anyMatch(s -> s.scare() == TerrorDirector.Scare.POWER_SURGE));
        FuseBox.Circuit tripped = office.getFuseBox().getTripped();
        run(director, 300);
        assertEquals(tripped, office.getFuseBox().getTripped());
    }

    @Test
    void exposesItsStateAndRejectsBadInput() {
        TerrorDirector director = new TerrorDirector(() -> 20);
        director.reset();
        assertNull(director.getLast());
        assertTrue(director.getTimeUntilNext() > 0);
        assertThrows(IllegalArgumentException.class, () -> director.update(-1, context));
        assertThrows(NullPointerException.class, () -> new TerrorDirector(null));
    }

    @Test
    void firstScareOfTheNightIsAlwaysAVoice() {
        for (int roll = 1; roll <= 20; roll++) {
            int value = roll;
            scares.clear();
            TerrorDirector director = new TerrorDirector(() -> value);
            director.reset();
            run(director, director.intervalAt(0));
            TerrorDirector.Scare first = scares.get(0).scare();
            assertTrue(first == TerrorDirector.Scare.LITTLE_GIRL || first == TerrorDirector.Scare.DISTANT_LAUGH);
        }
    }
}
