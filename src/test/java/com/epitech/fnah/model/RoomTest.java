package com.epitech.fnah.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void camerasAreNumberedOneToEight() {
        List<Room> cameras = Room.withCameras();
        assertEquals(Room.CAMERA_COUNT, cameras.size());
        for (int i = 0; i < cameras.size(); i++) {
            assertEquals(i + 1, cameras.get(i).getCameraNumber());
            assertEquals("CAM " + (i + 1), cameras.get(i).getCameraLabel());
        }
    }

    @Test
    void officeHasNoCamera() {
        assertFalse(Room.REGIE.hasCamera());
        assertEquals(0, Room.REGIE.getCameraNumber());
        assertNull(Room.REGIE.getCameraLabel());
        assertFalse(Room.withCameras().contains(Room.REGIE));
    }

    @Test
    void findsRoomFromCameraNumber() {
        assertEquals(Optional.of(Room.COULOIR_GAUCHE), Room.fromCameraNumber(4));
        assertEquals(Optional.empty(), Room.fromCameraNumber(0));
        assertEquals(Optional.empty(), Room.fromCameraNumber(9));
    }
}
