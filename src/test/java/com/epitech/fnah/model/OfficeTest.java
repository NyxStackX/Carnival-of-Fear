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
        office.getRightDoor().toggle();
        office.reset();
        assertFalse(office.getLeftDoor().isClosed());
        assertFalse(office.getRightDoor().isClosed());
    }

    @Test
    void closedDoorDrainsPowerThroughPowerSystem() {
        Office office = new Office();
        PowerSystem power = new PowerSystem();
        power.addConsumer(office);
        office.getLeftDoor().toggle();
        assertEquals(2, power.computeUsage());
    }

    @Test
    void doorAndTabletAddUp() {
        Office office = new Office();
        office.getLeftDoor().toggle();
        office.getCameras().toggleTablet();
        assertEquals(2, office.getPowerUsage());
    }

    @Test
    void bothDoorsAddToPowerUsage() {
        Office office = new Office();
        office.getLeftDoor().toggle();
        office.getRightDoor().toggle();
        assertEquals(2, office.getPowerUsage());
    }

    @Test
    void resetLowersTablet() {
        Office office = new Office();
        office.getCameras().toggleTablet();
        office.getCameras().select(Room.LOGES);
        office.reset();
        assertFalse(office.getCameras().isOpen());
        assertEquals(CameraSystem.DEFAULT_CAMERA, office.getCameras().getSelected());
    }
}
