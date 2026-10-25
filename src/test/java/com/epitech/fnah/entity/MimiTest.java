package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameClock;
import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;
import com.epitech.fnah.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MimiTest {

    private final Dice alwaysAct = () -> 1;
    private Office office;
    private GameContext context;
    private List<String> attacks;

    @BeforeEach
    void setUp() {
        office = new Office();
        attacks = new ArrayList<>();
        context = new GameContext(new GameClock(), new PowerSystem(), office, MapGraph.circus(), attacks::add);
    }

    @Test
    void followsTheRightRouteAndWaitsWhenObserved() {
        Mimi mimi = new Mimi(20, alwaysAct);
        office.getCameras().toggleTablet();
        office.getCameras().select(Room.LOGES);

        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Room.LOGES, mimi.getRoom());

        office.getCameras().toggleTablet();
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Room.MENAGERIE, mimi.getRoom());

        office.getCameras().toggleTablet();
        office.getCameras().select(Room.MENAGERIE);
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Room.MENAGERIE, mimi.getRoom());

        office.getCameras().toggleTablet();
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Room.COULOIR_DROIT, mimi.getRoom());
        assertFalse(mimi.isAtEmbrasure());
        assertFalse(office.getRightDoor().isBlocked());
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertTrue(mimi.isAtEmbrasure());
        assertTrue(office.getRightDoor().isBlocked());
    }

    @Test
    void retreatsWhenWatchedAndAttacksAfterThreeSecondsOtherwise() {
        Mimi mimi = new Mimi(20, alwaysAct);
        mimi.update(Mimi.ACTION_INTERVAL * 2, context);
        assertEquals(Room.COULOIR_DROIT, mimi.getRoom());
        office.getCameras().toggleTablet();
        office.getCameras().select(Room.COULOIR_DROIT);
        mimi.update(0.1, context);
        assertEquals(Room.COULOIR_DROIT, mimi.getRoom());
        office.getCameras().toggleTablet();
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertTrue(mimi.isAtEmbrasure());
        office.getCameras().toggleTablet();
        office.getCameras().select(Room.COULOIR_DROIT);
        mimi.update(0.1, context);
        assertEquals(Mimi.RETREAT_ROOM, mimi.getRoom());
        assertFalse(office.getRightDoor().isBlocked());
        assertTrue(attacks.isEmpty());

        mimi = new Mimi(20, alwaysAct);
        office.getCameras().toggleTablet();
        mimi.update(Mimi.ACTION_INTERVAL, context);
        mimi.update(Mimi.ACTION_INTERVAL, context);
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Room.COULOIR_DROIT, mimi.getRoom());
        assertTrue(mimi.isAtEmbrasure());
        mimi.update(Mimi.EMBRASURE_TIMEOUT, context);
        assertEquals(List.of(Mimi.NAME), attacks);
        assertEquals(Room.REGIE, mimi.getRoom());
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(1, attacks.size());
    }

    @Test
    void closedRightDoorRepelsMimi() {
        Mimi mimi = new Mimi(20, alwaysAct);
        mimi.update(Mimi.ACTION_INTERVAL, context);
        office.getRightDoor().toggle();
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Room.LOGES, mimi.getRoom());

        office.getRightDoor().toggle();
        mimi.update(Mimi.ACTION_INTERVAL, context);
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Room.COULOIR_DROIT, mimi.getRoom());
        office.getRightDoor().toggle();
        mimi.update(Mimi.ACTION_INTERVAL, context);
        assertEquals(Mimi.RETREAT_ROOM, mimi.getRoom());
        assertFalse(mimi.isAtEmbrasure());
    }
}
