package com.epitech.fnah.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CameraSystemTest {

    private CameraSystem cameras;

    @BeforeEach
    void setUp() {
        cameras = new CameraSystem();
    }

    @Test
    void startsClosedOnDefaultCamera() {
        assertFalse(cameras.isOpen());
        assertEquals(CameraSystem.DEFAULT_CAMERA, cameras.getSelected());
        assertEquals(0, cameras.getPowerUsage());
    }

    @Test
    void openTabletConsumesPower() {
        cameras.toggleTablet();
        assertTrue(cameras.isOpen());
        assertEquals(CameraSystem.POWER_USAGE_WHEN_OPEN, cameras.getPowerUsage());
        cameras.toggleTablet();
        assertEquals(0, cameras.getPowerUsage());
    }

    @Test
    void selectChangesCamera() {
        cameras.select(Room.COULOIR_GAUCHE);
        assertEquals(Room.COULOIR_GAUCHE, cameras.getSelected());
    }

    @Test
    void officeHasNoCamera() {
        assertThrows(IllegalArgumentException.class, () -> cameras.select(Room.REGIE));
        assertThrows(NullPointerException.class, () -> cameras.select(null));
    }

    @Test
    void watchingNeedsOpenTabletOnThatRoom() {
        cameras.select(Room.GRADINS);
        assertFalse(cameras.isWatching(Room.GRADINS));
        cameras.toggleTablet();
        assertTrue(cameras.isWatching(Room.GRADINS));
        assertFalse(cameras.isWatching(Room.LOGES));
    }

    @Test
    void cycleWrapsAround() {
        cameras.cycle(-1);
        assertEquals(Room.FOSSE_ORCHESTRE, cameras.getSelected());
        cameras.cycle(1);
        assertEquals(Room.PISTE_CENTRALE, cameras.getSelected());
        cameras.cycle(1);
        assertEquals(Room.GRADINS, cameras.getSelected());
    }

    @Test
    void selectionListenerFiresOnlyOnChange() {
        List<Room> seen = new ArrayList<>();
        cameras.onCameraSelected(c -> seen.add(c.getSelected()));
        cameras.select(Room.GRADINS);
        cameras.select(Room.GRADINS);
        cameras.select(Room.LOGES);
        assertEquals(List.of(Room.GRADINS, Room.LOGES), seen);
    }

    @Test
    void tabletListenerFiresOnToggle() {
        List<Boolean> seen = new ArrayList<>();
        cameras.onTabletToggle(c -> seen.add(c.isOpen()));
        cameras.toggleTablet();
        cameras.toggleTablet();
        assertEquals(List.of(true, false), seen);
    }

    @Test
    void resetClosesAndReturnsToDefault() {
        cameras.toggleTablet();
        cameras.select(Room.MENAGERIE);
        cameras.reset();
        assertFalse(cameras.isOpen());
        assertEquals(CameraSystem.DEFAULT_CAMERA, cameras.getSelected());
    }

    @Test
    void rejectsNullListeners() {
        assertThrows(NullPointerException.class, () -> cameras.onTabletToggle(null));
        assertThrows(NullPointerException.class, () -> cameras.onCameraSelected(null));
    }

    @Test
    void disabledCameraShowsStaticAndExpiresAfterItsTimer() {
        cameras.disable(Room.GRADINS, 15);
        cameras.toggleTablet();
        cameras.select(Room.GRADINS);
        assertTrue(cameras.isDisabled(Room.GRADINS));
        assertFalse(cameras.isWatching(Room.GRADINS));

        cameras.update(4);
        assertEquals(11, cameras.getDisabledTimeRemaining(Room.GRADINS));
        cameras.update(11);
        assertFalse(cameras.isDisabled(Room.GRADINS));
        assertTrue(cameras.isWatching(Room.GRADINS));
    }

    @Test
    void rejectsInvalidCameraDisruption() {
        assertThrows(IllegalArgumentException.class, () -> cameras.disable(Room.REGIE, 1));
        assertThrows(IllegalArgumentException.class, () -> cameras.disable(Room.GRADINS, 0));
        assertThrows(IllegalArgumentException.class, () -> cameras.update(-1));
    }
}
