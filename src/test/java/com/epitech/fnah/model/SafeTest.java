package com.epitech.fnah.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SafeTest {

    @Test
    void rightCodeOpensTheSafe() {
        Safe safe = new Safe();
        safe.setCode(4, 0, 7);
        assertEquals(Safe.Result.PARTIAL, safe.press(4));
        assertEquals(1, safe.getTypedCount());
        assertEquals(Safe.Result.PARTIAL, safe.press(0));
        assertEquals(Safe.Result.OPENED, safe.press(7));
        assertTrue(safe.isOpen());
        assertEquals(Safe.Result.ALREADY_OPEN, safe.press(1));
    }

    @Test
    void wrongCodeClearsTheEntry() {
        Safe safe = new Safe();
        safe.setCode(1, 2, 3);
        safe.press(1);
        safe.press(2);
        assertEquals(Safe.Result.WRONG, safe.press(4));
        assertEquals(0, safe.getTypedCount());
        assertFalse(safe.isOpen());
        safe.press(9);
        safe.clearEntry();
        assertEquals(0, safe.getTypedCount());
    }

    @Test
    void cluesAreDiscoveredOnce() {
        Safe safe = new Safe();
        safe.setCode(5, 6, 8);
        assertFalse(safe.isDiscovered(1));
        assertTrue(safe.discover(1));
        assertFalse(safe.discover(1));
        assertTrue(safe.isDiscovered(1));
        assertEquals(6, safe.digit(1));
    }

    @Test
    void newCodeForgetsCluesAndClosesTheSafe() {
        Safe safe = new Safe();
        safe.setCode(0, 0, 0);
        safe.discover(0);
        safe.press(0);
        safe.press(0);
        safe.press(0);
        safe.setCode(1, 1, 1);
        assertFalse(safe.isOpen());
        assertFalse(safe.isDiscovered(0));
    }

    @Test
    void cluesAreWrittenInThreeRooms() {
        assertEquals(0, Safe.clueIndex(Room.GRADINS));
        assertEquals(1, Safe.clueIndex(Room.LOGES));
        assertEquals(2, Safe.clueIndex(Room.MENAGERIE));
        assertEquals(-1, Safe.clueIndex(Room.REGIE));
    }

    @Test
    void rejectsInvalidValues() {
        Safe safe = new Safe();
        assertThrows(IllegalArgumentException.class, () -> safe.setCode(1, 2));
        assertThrows(IllegalArgumentException.class, () -> safe.setCode(1, 2, 10));
        assertThrows(NullPointerException.class, () -> safe.setCode((int[]) null));
        assertThrows(IllegalArgumentException.class, () -> safe.press(-1));
        assertThrows(IllegalArgumentException.class, () -> safe.discover(3));
        assertThrows(IllegalArgumentException.class, () -> safe.digit(-1));
    }
}
