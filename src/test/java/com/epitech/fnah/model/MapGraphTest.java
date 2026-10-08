package com.epitech.fnah.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MapGraphTest {

    private MapGraph map;

    @BeforeEach
    void setUp() {
        map = MapGraph.circus();
    }

    @Test
    void circusMatchesGddTable() {
        assertEquals(List.of(Room.GRADINS, Room.LOGES), map.neighbors(Room.PISTE_CENTRALE));
        assertEquals(List.of(Room.COULOIR_GAUCHE, Room.COULOIR_DROIT, Room.FOSSE_ORCHESTRE), map.neighbors(Room.REGIE));
        assertEquals(List.of(Room.REGIE), map.neighbors(Room.FOSSE_ORCHESTRE));
    }

    @Test
    void connectionsGoBothWays() {
        assertTrue(map.areConnected(Room.GRADINS, Room.COULISSES_OUEST));
        assertTrue(map.areConnected(Room.COULISSES_OUEST, Room.GRADINS));
        assertFalse(map.areConnected(Room.GRADINS, Room.LOGES));
    }

    @Test
    void shortestPathFromStageToLeftCorridor() {
        assertEquals(List.of(Room.PISTE_CENTRALE, Room.GRADINS, Room.COULISSES_OUEST, Room.COULOIR_GAUCHE),
                map.shortestPath(Room.PISTE_CENTRALE, Room.COULOIR_GAUCHE));
    }

    @Test
    void shortestPathFromStageToRightCorridor() {
        assertEquals(List.of(Room.PISTE_CENTRALE, Room.LOGES, Room.MENAGERIE, Room.COULOIR_DROIT),
                map.shortestPath(Room.PISTE_CENTRALE, Room.COULOIR_DROIT));
    }

    @Test
    void pathsNeverCrossTheOffice() {
        // Sans la régie, aller du couloir gauche au couloir droit fait tout le tour du chapiteau.
        List<Room> path = map.shortestPath(Room.COULOIR_GAUCHE, Room.COULOIR_DROIT);
        assertFalse(path.contains(Room.REGIE));
        assertEquals(7, path.size());
    }

    @Test
    void officeCanBeReachedButNotCrossed() {
        assertEquals(1, map.distance(Room.COULOIR_GAUCHE, Room.REGIE));
        assertEquals(-1, map.distance(Room.FOSSE_ORCHESTRE, Room.PISTE_CENTRALE));
    }

    @Test
    void pathFromOfficeCanLeaveIt() {
        assertEquals(List.of(Room.REGIE, Room.COULOIR_GAUCHE), map.shortestPath(Room.REGIE, Room.COULOIR_GAUCHE));
    }

    @Test
    void sameRoomPathIsSingleRoom() {
        assertEquals(List.of(Room.GRADINS), map.shortestPath(Room.GRADINS, Room.GRADINS));
        assertEquals(0, map.distance(Room.GRADINS, Room.GRADINS));
        assertEquals(Optional.empty(), map.nextStepTowards(Room.GRADINS, Room.GRADINS));
    }

    @Test
    void nextStepFollowsShortestPath() {
        assertEquals(Optional.of(Room.GRADINS), map.nextStepTowards(Room.PISTE_CENTRALE, Room.COULOIR_GAUCHE));
    }

    @Test
    void unreachableRoomGivesEmptyPath() {
        MapGraph empty = new MapGraph();
        assertEquals(List.of(), empty.shortestPath(Room.GRADINS, Room.REGIE));
        assertEquals(Optional.empty(), empty.nextStepTowards(Room.GRADINS, Room.REGIE));
    }

    @Test
    void connectingTwiceAddsOnePassage() {
        map.connect(Room.GRADINS, Room.PISTE_CENTRALE);
        assertEquals(2, map.neighbors(Room.PISTE_CENTRALE).size());
    }

    @Test
    void rejectsSelfConnection() {
        assertThrows(IllegalArgumentException.class, () -> map.connect(Room.GRADINS, Room.GRADINS));
    }

    @Test
    void neighborsAreReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> map.neighbors(Room.GRADINS).add(Room.LOGES));
    }

    @Test
    void everyRoomButOfficeHasACamera() {
        for (Room room : Room.values()) {
            if (room == Room.REGIE) {
                assertNull(room.getCameraLabel());
            } else {
                assertTrue(room.getCameraLabel().startsWith("CAM "));
                assertFalse(room.getDisplayName().isBlank());
            }
        }
    }
}
