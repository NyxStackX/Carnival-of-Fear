package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameClock;
import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HenryTest {

    private PowerSystem power;
    private GameContext context;
    private List<String> attacks;
    private List<GameEvent> events;

    @BeforeEach
    void setUp() {
        power = new PowerSystem(1000);
        attacks = new ArrayList<>();
        events = new ArrayList<>();
        context = new GameContext(new GameClock(), power, new Office(), MapGraph.circus(), attacks::add, events::add);
    }

    @Test
    void waitsWhileThePowerIsOn() {
        Henry henry = new Henry(() -> 1);
        henry.update(100, context);
        assertEquals(Henry.Phase.WAITING, henry.getPhase());
        assertFalse(henry.isPlayingMelody());
    }

    @Test
    void darkThenMelodyThenAttack() {
        Henry henry = new Henry(() -> 2);
        power.update(10);
        assertTrue(power.isPowerOut());
        henry.update(0, context);
        assertEquals(Henry.Phase.DARK, henry.getPhase());

        double dark = Henry.DARK_BASE + 2 * Henry.SECONDS_PER_ROLL;
        henry.update(dark - 0.1, context);
        assertEquals(Henry.Phase.DARK, henry.getPhase());
        henry.update(0.1, context);
        assertTrue(henry.isPlayingMelody());
        assertEquals(List.of(new GameEvent.HenryMelody(true)), events);

        double melody = Henry.MELODY_BASE + 2 * Henry.SECONDS_PER_ROLL;
        henry.update(melody, context);
        assertEquals(Henry.Phase.ATTACKED, henry.getPhase());
        assertEquals(List.of(Henry.NAME), attacks);
        assertTrue(events.contains(new GameEvent.HenryMelody(false)));

        henry.update(100, context);
        assertEquals(1, attacks.size());
    }

    @Test
    void resetGoesBackToWaiting() {
        Henry henry = new Henry(() -> 1);
        power.update(10);
        henry.update(0, context);
        henry.reset();
        assertEquals(Henry.Phase.WAITING, henry.getPhase());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(NullPointerException.class, () -> new Henry(null));
        assertThrows(IllegalArgumentException.class, () -> new Henry(() -> 1).update(-1, context));
    }
}
