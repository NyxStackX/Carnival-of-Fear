package com.epitech.fnah.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfficeTest {

    @Test
    void usageFollowsLeftDoor() {
        Office office = new Office();
        assertEquals(0, office.getPowerUsage());
        office.getLeftDoor().toggle();
        assertEquals(1, office.getPowerUsage());
    }

    @Test
    void resetOpensDoors() {
        Office office = new Office();
        office.getLeftDoor().toggle();
        office.reset();
        assertFalse(office.getLeftDoor().isClosed());
    }

    @Test
    void closedDoorDrainsPowerThroughPowerSystem() {
        Office office = new Office();
        PowerSystem power = new PowerSystem();
        power.addConsumer(office);
        office.getLeftDoor().toggle();
        assertEquals(2, power.computeUsage());
    }
}
