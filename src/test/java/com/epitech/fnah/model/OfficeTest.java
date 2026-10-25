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

    @Test
    void lightsAddToThePowerUsage() {
        Office office = new Office();
        office.getLeftLight().setOn(true);
        office.getRightLight().setOn(true);
        assertEquals(2 * Light.POWER_USAGE_WHEN_ON, office.getPowerUsage());
    }

    @Test
    void cutPowerShutsEverythingDown() {
        Office office = new Office();
        office.getLeftDoor().toggle();
        office.getRightDoor().toggle();
        office.getLeftLight().setOn(true);
        office.getOrgan().setWinding(true);
        office.getCameras().toggleTablet();
        office.cutPower();
        assertFalse(office.getLeftDoor().isClosed());
        assertFalse(office.getRightDoor().isClosed());
        assertFalse(office.getLeftLight().isOn());
        assertFalse(office.getOrgan().isWinding());
        assertFalse(office.getCameras().isOpen());
        assertEquals(0, office.getPowerUsage());
    }

    @Test
    void cutPowerWithTabletDownKeepsItDown() {
        Office office = new Office();
        office.cutPower();
        assertFalse(office.getCameras().isOpen());
    }

    @Test
    void resetRefillsTheOrganAndSwitchesLightsOff() {
        Office office = new Office();
        office.getOrgan().drain(60);
        office.getRightLight().setOn(true);
        office.reset();
        assertEquals(BarrelOrgan.MAX_CHARGE, office.getOrgan().getCharge());
        assertFalse(office.getRightLight().isOn());
    }

    @Test
    void holdingTheFuseLeverMakesNoise() {
        Office office = new Office();
        office.getFuseBox().trip(FuseBox.Circuit.LIGHTS);
        office.getFuseBox().setResetting(true);
        office.update(1.0);
        assertTrue(office.getNoise().getLevel() > 0);
        office.update(FuseBox.RESET_TIME);
        assertNull(office.getFuseBox().getTripped());
    }

    @Test
    void resetClearsNoiseFusesAndHiding() {
        Office office = new Office();
        office.getNoise().make(80);
        office.getFuseBox().trip(FuseBox.Circuit.CAMERAS);
        office.setHidden(true);
        assertTrue(office.isHidden());
        office.reset();
        assertEquals(0.0, office.getNoise().getLevel());
        assertNull(office.getFuseBox().getTripped());
        assertFalse(office.isHidden());
        assertNotNull(office.getSafe());
    }
}
