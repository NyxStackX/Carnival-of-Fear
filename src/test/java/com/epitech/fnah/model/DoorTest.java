package com.epitech.fnah.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DoorTest {

    private Door door;

    @BeforeEach
    void setUp() {
        door = new Door(Door.Side.LEFT);
    }

    @Test
    void startsOpenAndFree() {
        assertFalse(door.isClosed());
        assertEquals(0, door.getPowerUsage());
        assertEquals(Door.Side.LEFT, door.getSide());
        assertEquals("Porte gauche", door.getSide().getDisplayName());
        assertEquals(Room.COULOIR_GAUCHE, door.getSide().getCorridor());
    }

    @Test
    void toggleClosesThenOpens() {
        door.toggle();
        assertTrue(door.isClosed());
        door.toggle();
        assertFalse(door.isClosed());
    }

    @Test
    void closedDoorConsumesPower() {
        door.toggle();
        assertEquals(Door.POWER_USAGE_WHEN_CLOSED, door.getPowerUsage());
    }

    @Test
    void resetOpensWithoutNotifying() {
        AtomicInteger count = new AtomicInteger();
        door.toggle();
        door.onToggle(d -> count.incrementAndGet());
        door.reset();
        assertFalse(door.isClosed());
        assertEquals(0, count.get());
    }

    @Test
    void toggleListenersFire() {
        AtomicInteger count = new AtomicInteger();
        door.onToggle(d -> count.incrementAndGet());
        door.toggle();
        door.toggle();
        assertEquals(2, count.get());
    }

    @Test
    void rejectsNullSide() {
        assertThrows(NullPointerException.class, () -> new Door(null));
    }

    @Test
    void blockedDoorCannotBeToggledAndResetUnblocksIt() {
        Door rightDoor = new Door(Door.Side.RIGHT);
        rightDoor.setBlocked(true);
        rightDoor.toggle();
        assertFalse(rightDoor.isClosed());
        assertTrue(rightDoor.isBlocked());

        rightDoor.reset();
        assertFalse(rightDoor.isBlocked());
        rightDoor.toggle();
        assertTrue(rightDoor.isClosed());
    }
}
