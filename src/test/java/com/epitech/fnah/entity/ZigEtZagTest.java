package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameClock;
import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;
import com.epitech.fnah.model.Room;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZigEtZagTest {

    @Test
    void wandersWithoutEnteringTheOfficeAndCanPlayAFakeHorn() {
        Office office = new Office();
        List<GameEvent> events = new ArrayList<>();
        GameContext context = context(office, events);
        ZigEtZag twins = new ZigEtZag(20, sequence(1, 1, 1));

        twins.update(ZigEtZag.ACTION_INTERVAL, context);

        assertNotEquals(Room.REGIE, twins.getRoom());
        assertEquals(List.of(new GameEvent.FakeHornPlayed()), events);
    }

    @Test
    void canDisableACameraAndWatchingThemForTwoSecondsSendsThemToTheStands() {
        Office office = new Office();
        GameContext context = context(office, new ArrayList<>());
        ZigEtZag twins = new ZigEtZag(20, sequence(1, 1, 2, 8));

        twins.update(ZigEtZag.ACTION_INTERVAL, context);
        assertEquals(Room.COULISSES_OUEST, twins.getRoom());
        assertTrue(office.getCameras().isDisabled(Room.PISTE_CENTRALE));
        assertEquals(ZigEtZag.CAMERA_DISABLE_DURATION,
                office.getCameras().getDisabledTimeRemaining(Room.PISTE_CENTRALE));

        office.getCameras().toggleTablet();
        office.getCameras().select(Room.COULISSES_OUEST);
        twins.update(ZigEtZag.WATCH_TO_FLEE_DURATION, context);
        assertEquals(ZigEtZag.START_ROOM, twins.getRoom());

        office.getCameras().update(ZigEtZag.CAMERA_DISABLE_DURATION);
        assertFalse(office.getCameras().isDisabled(Room.PISTE_CENTRALE));
    }

    private static GameContext context(Office office, List<GameEvent> events) {
        return new GameContext(new GameClock(), new PowerSystem(), office, MapGraph.circus(),
                attacker -> fail("Zig et Zag must never attack"), events::add);
    }

    private static Dice sequence(int... values) {
        Deque<Integer> rolls = new ArrayDeque<>();
        for (int value : values) {
            rolls.add(value);
        }
        return () -> rolls.removeFirst();
    }
}
