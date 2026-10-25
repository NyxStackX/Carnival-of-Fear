package com.epitech.fnah.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarrelOrganTest {

    @Test
    void startsFull() {
        BarrelOrgan organ = new BarrelOrgan();
        assertEquals(BarrelOrgan.MAX_CHARGE, organ.getCharge());
        assertFalse(organ.isEmpty());
        assertFalse(organ.isWinding());
    }

    @Test
    void drainsDownToZeroOnly() {
        BarrelOrgan organ = new BarrelOrgan();
        organ.drain(30);
        assertEquals(70.0, organ.getCharge(), 1e-9);
        organ.drain(500);
        assertEquals(0.0, organ.getCharge());
        assertTrue(organ.isEmpty());
    }

    @Test
    void windingRefillsUpToMax() {
        BarrelOrgan organ = new BarrelOrgan();
        organ.drain(100);
        organ.wind(1.0);
        assertEquals(BarrelOrgan.WIND_RATE, organ.getCharge(), 1e-9);
        organ.wind(100.0);
        assertEquals(BarrelOrgan.MAX_CHARGE, organ.getCharge());
    }

    @Test
    void resetRefillsAndReleasesTheCrank() {
        BarrelOrgan organ = new BarrelOrgan();
        organ.drain(80);
        organ.setWinding(true);
        assertTrue(organ.isWinding());
        organ.reset();
        assertEquals(BarrelOrgan.MAX_CHARGE, organ.getCharge());
        assertFalse(organ.isWinding());
    }

    @Test
    void rejectsNegativeValues() {
        BarrelOrgan organ = new BarrelOrgan();
        assertThrows(IllegalArgumentException.class, () -> organ.wind(-1));
        assertThrows(IllegalArgumentException.class, () -> organ.drain(-1));
    }
}
