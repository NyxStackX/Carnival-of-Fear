package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameClock;
import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.model.BarrelOrgan;
import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;
import com.epitech.fnah.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PepinTest {

    private final Dice dice = () -> 1;
    private Office office;
    private GameContext context;
    private List<String> attacks;
    private List<GameEvent> events;

    @BeforeEach
    void setUp() {
        office = new Office();
        attacks = new ArrayList<>();
        events = new ArrayList<>();
        context = new GameContext(new GameClock(), new PowerSystem(), office, MapGraph.circus(),
                attacks::add, events::add);
    }

    private void watchOrgan() {
        office.getCameras().toggleTablet();
        office.getCameras().select(Pepin.START_ROOM);
    }

    @Test
    void startsInItsBoxInTheOrchestraPit() {
        Pepin pepin = new Pepin(5, dice);
        assertEquals(Room.FOSSE_ORCHESTRE, pepin.getRoom());
        assertFalse(pepin.isOut());
        assertEquals(0.0, pepin.getEscapeTimeRemaining());
    }

    @Test
    void drainsTheOrganFasterWithAHigherLevel() {
        new Pepin(10, dice).update(10, context);
        assertEquals(BarrelOrgan.MAX_CHARGE - 10 * Pepin.DRAIN_PER_LEVEL * 10, office.getOrgan().getCharge(), 1e-9);
    }

    @Test
    void sleepingPepinNeverDrainsTheOrgan() {
        new Pepin(0, dice).update(1000, context);
        assertEquals(BarrelOrgan.MAX_CHARGE, office.getOrgan().getCharge());
    }

    @Test
    void windingRefillsOnlyWhileWatchingCameraEight() {
        Pepin pepin = new Pepin(10, dice);
        office.getOrgan().drain(50);
        office.getOrgan().setWinding(true);
        pepin.update(1.0, context);
        assertTrue(office.getOrgan().getCharge() < 50);
        watchOrgan();
        pepin.update(1.0, context);
        assertTrue(office.getOrgan().getCharge() > 50);
    }

    @Test
    void escapesWhenTheOrganStopsThenAttacks() {
        Pepin pepin = new Pepin(20, dice);
        office.getOrgan().drain(BarrelOrgan.MAX_CHARGE);
        pepin.update(0.1, context);
        assertTrue(pepin.isOut());
        assertTrue(events.contains(new GameEvent.PepinEscaped(true)));
        assertEquals(Pepin.ESCAPE_DELAY, pepin.getEscapeTimeRemaining(), 1e-9);
        office.getLeftDoor().toggle();
        office.getRightDoor().toggle();
        pepin.update(Pepin.ESCAPE_DELAY, context);
        assertEquals(List.of(Pepin.NAME), attacks);
        assertEquals(Room.REGIE, pepin.getRoom());
        assertFalse(pepin.isOut());
    }

    @Test
    void windingBackInTimeSendsHimHome() {
        Pepin pepin = new Pepin(20, dice);
        office.getOrgan().drain(BarrelOrgan.MAX_CHARGE);
        pepin.update(0.1, context);
        watchOrgan();
        office.getOrgan().setWinding(true);
        for (int i = 0; i < 20 && pepin.isOut(); i++) {
            pepin.update(0.1, context);
        }
        assertFalse(pepin.isOut());
        assertTrue(events.contains(new GameEvent.PepinEscaped(false)));
        assertTrue(attacks.isEmpty());
    }

    @Test
    void resetPutsHimBackInTheBox() {
        Pepin pepin = new Pepin(20, dice);
        office.getOrgan().drain(BarrelOrgan.MAX_CHARGE);
        pepin.update(0.1, context);
        pepin.reset();
        assertFalse(pepin.isOut());
        assertEquals(Pepin.START_ROOM, pepin.getRoom());
    }

    @Test
    void hidingSilentlyMakesHimGiveUp() {
        Pepin pepin = new Pepin(20, dice);
        office.getOrgan().drain(BarrelOrgan.MAX_CHARGE);
        pepin.update(0.1, context);
        office.setHidden(true);
        pepin.update(Pepin.ESCAPE_DELAY, context);
        assertTrue(pepin.isSearching());
        assertTrue(attacks.isEmpty());
        pepin.update(Pepin.SEARCH_TIME, context);
        assertFalse(pepin.isOut());
        assertFalse(pepin.isSearching());
        assertTrue(office.getOrgan().getCharge() >= Pepin.RETURN_THRESHOLD - 1e-9);
        assertTrue(attacks.isEmpty());
    }

    @Test
    void leavingTheHidingPlaceDuringTheSearchIsFatal() {
        Pepin pepin = new Pepin(20, dice);
        office.getOrgan().drain(BarrelOrgan.MAX_CHARGE);
        pepin.update(0.1, context);
        office.setHidden(true);
        pepin.update(Pepin.ESCAPE_DELAY, context);
        office.setHidden(false);
        pepin.update(0.1, context);
        assertEquals(List.of(Pepin.NAME), attacks);
    }

    @Test
    void noiseDuringTheSearchGivesYouAway() {
        Pepin pepin = new Pepin(20, dice);
        office.getOrgan().drain(BarrelOrgan.MAX_CHARGE);
        pepin.update(0.1, context);
        office.setHidden(true);
        pepin.update(Pepin.ESCAPE_DELAY, context);
        office.getNoise().make(100);
        pepin.update(0.1, context);
        assertEquals(List.of(Pepin.NAME), attacks);
    }

    @Test
    void windingIsNoisy() {
        Pepin pepin = new Pepin(5, dice);
        watchOrgan();
        office.getOrgan().setWinding(true);
        pepin.update(1.0, context);
        assertEquals(Pepin.CRANK_NOISE_PER_SECOND, office.getNoise().getLevel(), 1e-9);
    }
}
