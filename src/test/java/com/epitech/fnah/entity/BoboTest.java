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

class BoboTest {

    private static final Dice ALWAYS_ACT = () -> 1;
    private static final Dice NEVER_ACT = () -> 20;
    private static final double STEP = Bobo.ACTION_INTERVAL;

    private GameClock clock;
    private Office office;
    private GameContext ctx;
    private List<String> attacks;

    @BeforeEach
    void setUp() {
        clock = new GameClock();
        office = new Office();
        attacks = new ArrayList<>();
        ctx = new GameContext(clock, new PowerSystem(), office, MapGraph.circus(), attacks::add);
    }

    @Test
    void startsOnCentralRing() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        assertEquals(Room.PISTE_CENTRALE, bobo.getRoom());
        assertEquals("Bobo", bobo.getName());
        assertFalse(bobo.isAtDoor());
    }

    @Test
    void waitsForFullInterval() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP - 0.1, ctx);
        assertEquals(Room.PISTE_CENTRALE, bobo.getRoom());
        bobo.update(0.1, ctx);
        assertEquals(Room.GRADINS, bobo.getRoom());
    }

    @Test
    void doesNotMoveWhenRollFails() {
        Bobo bobo = new Bobo(3, NEVER_ACT);
        bobo.update(STEP * 10, ctx);
        assertEquals(Room.PISTE_CENTRALE, bobo.getRoom());
    }

    @Test
    void levelZeroNeverMoves() {
        Bobo bobo = new Bobo(0, ALWAYS_ACT);
        bobo.update(STEP * 10, ctx);
        assertEquals(Room.PISTE_CENTRALE, bobo.getRoom());
    }

    @Test
    void followsPathToLeftCorridor() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP, ctx);
        assertEquals(Room.GRADINS, bobo.getRoom());
        bobo.update(STEP, ctx);
        assertEquals(Room.COULISSES_OUEST, bobo.getRoom());
        bobo.update(STEP, ctx);
        assertEquals(Room.COULOIR_GAUCHE, bobo.getRoom());
        assertTrue(bobo.isAtDoor());
        assertTrue(attacks.isEmpty());
    }

    @Test
    void attacksWhenDoorIsOpen() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP * 4, ctx);
        assertEquals(Room.REGIE, bobo.getRoom());
        assertEquals(List.of("Bobo"), attacks);
    }

    @Test
    void retreatsWhenDoorIsClosed() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP * 3, ctx);
        office.getLeftDoor().toggle();
        bobo.update(STEP, ctx);
        assertEquals(Bobo.RETREAT_ROOM, bobo.getRoom());
        assertTrue(attacks.isEmpty());
    }

    @Test
    void doesNothingOnceInOffice() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP * 4, ctx);
        bobo.update(STEP * 3, ctx);
        assertEquals(Room.REGIE, bobo.getRoom());
        assertEquals(1, attacks.size());
    }

    @Test
    void aiLevelRisesAtTwoAndFour() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        assertEquals(3, bobo.getAiLevel(0));
        assertEquals(3, bobo.getAiLevel(1));
        assertEquals(4, bobo.getAiLevel(2));
        assertEquals(5, bobo.getAiLevel(4));
    }

    @Test
    void inactiveStaysInactiveAllNight() {
        assertEquals(0, new Bobo(0, ALWAYS_ACT).getAiLevel(5));
    }

    @Test
    void aiLevelIsCappedAtMax() {
        assertEquals(Animatronic.MAX_AI_LEVEL, new Bobo(20, ALWAYS_ACT).getAiLevel(5));
    }

    @Test
    void hourBoostMakesHarderRollsSucceed() {
        Bobo bobo = new Bobo(3, () -> 4);
        bobo.update(STEP, ctx);
        assertEquals(Room.PISTE_CENTRALE, bobo.getRoom());
        clock.update(GameClock.SECONDS_PER_HOUR * 2.0); // 02:00, niveau 4
        bobo.update(STEP, ctx);
        assertEquals(Room.GRADINS, bobo.getRoom());
    }

    @Test
    void rejectsInvalidAiLevel() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        assertThrows(IllegalArgumentException.class, () -> bobo.setBaseAiLevel(-1));
        assertThrows(IllegalArgumentException.class, () -> bobo.setBaseAiLevel(21));
        assertThrows(IllegalArgumentException.class, () -> new Bobo(21, ALWAYS_ACT));
    }

    @Test
    void setBaseAiLevelChangesLevel() {
        Bobo bobo = new Bobo(0, ALWAYS_ACT);
        bobo.setBaseAiLevel(7);
        assertEquals(7, bobo.getBaseAiLevel());
    }

    @Test
    void rejectsNegativeDelta() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        assertThrows(IllegalArgumentException.class, () -> bobo.update(-1.0, ctx));
    }

    @Test
    void resetReturnsToStartAndClearsTimer() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP * 2 + 4.0, ctx);
        bobo.reset();
        assertEquals(Bobo.START_ROOM, bobo.getRoom());
        bobo.update(4.0, ctx);
        assertEquals(Bobo.START_ROOM, bobo.getRoom());
    }

    @Test
    void moveListenerReceivesFromAndTo() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        List<String> moves = new ArrayList<>();
        bobo.onMove((from, to) -> moves.add(from + ">" + to));
        bobo.update(STEP, ctx);
        assertEquals(List.of("PISTE_CENTRALE>GRADINS"), moves);
    }

    @Test
    void takesShortcutWhenMapHasOne() {
        MapGraph map = MapGraph.circus();
        map.connect(Room.PISTE_CENTRALE, Room.COULOIR_GAUCHE);
        GameContext shortcut = new GameContext(clock, new PowerSystem(), office, map, attacks::add);
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP, shortcut);
        assertEquals(Room.COULOIR_GAUCHE, bobo.getRoom());
    }

    @Test
    void staysPutWhenDoorIsUnreachable() {
        GameContext isolated = new GameContext(clock, new PowerSystem(), office, new MapGraph(), attacks::add);
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP * 5, isolated);
        assertEquals(Bobo.START_ROOM, bobo.getRoom());
    }

    @Test
    void rejectsNonPositiveInterval() {
        assertThrows(IllegalArgumentException.class, () -> new Animatronic("X", Room.GRADINS, 1, 0, ALWAYS_ACT) {
            @Override
            protected void act(GameContext c) {
            }
        });
    }

    /** Dé qui réussit tout jusqu'à ce que Bobo soit à la porte, puis échoue toujours. */
    private static Dice untilAtDoor(Bobo[] holder) {
        return () -> holder[0] != null && holder[0].isAtDoor() ? 20 : 1;
    }

    @Test
    void clownInFrontOfAnOpenDoorAlwaysKills() {
        Bobo[] holder = new Bobo[1];
        Bobo bobo = new Bobo(3, untilAtDoor(holder));
        holder[0] = bobo;
        bobo.update(STEP * 3, ctx);
        assertTrue(bobo.isAtDoor());
        bobo.update(Bobo.lurkTime(3) - 0.1, ctx);
        assertTrue(attacks.isEmpty());
        bobo.update(0.1, ctx);
        assertEquals(List.of("Bobo"), attacks);
        assertEquals(Room.REGIE, bobo.getRoom());
    }

    @Test
    void closedDoorAlwaysSendsHimBackAfterWaiting() {
        Bobo[] holder = new Bobo[1];
        Bobo bobo = new Bobo(3, untilAtDoor(holder));
        holder[0] = bobo;
        bobo.update(STEP * 3, ctx);
        office.getLeftDoor().toggle();
        bobo.update(Bobo.lurkTime(3), ctx);
        assertEquals(Bobo.RETREAT_ROOM, bobo.getRoom());
        assertTrue(bobo.isSearching());
        assertTrue(attacks.isEmpty());
    }

    @Test
    void lurkTimeShrinksWithLevel() {
        assertEquals(5.0, Bobo.lurkTime(0), 1e-9);
        assertTrue(Bobo.lurkTime(10) < Bobo.lurkTime(3));
        assertEquals(2.0, Bobo.lurkTime(20), 1e-9);
    }

    @Test
    void searchesBeforeHuntingAgain() {
        Bobo bobo = new Bobo(3, ALWAYS_ACT);
        bobo.update(STEP * 3, ctx);
        office.getLeftDoor().toggle();
        bobo.update(STEP, ctx);
        assertTrue(bobo.isSearching());
        bobo.update(STEP * Bobo.SEARCH_MOVES, ctx);
        assertFalse(bobo.isSearching());
        bobo.reset();
        assertFalse(bobo.isSearching());
    }

    @Test
    void sometimesWandersInsteadOfHunting() {
        Bobo bobo = new Bobo(3, () -> 15);
        bobo.setBaseAiLevel(20);
        bobo.update(STEP, ctx);
        assertNotEquals(Room.PISTE_CENTRALE, bobo.getRoom());
        assertNotEquals(Room.REGIE, bobo.getRoom());
    }

    @Test
    void noiseMakesHimBolder() {
        Bobo bobo = new Bobo(3, () -> 5);
        bobo.update(STEP, ctx);
        assertEquals(Room.PISTE_CENTRALE, bobo.getRoom());
        office.getNoise().make(100);
        bobo.update(STEP, ctx);
        assertEquals(Room.GRADINS, bobo.getRoom());
    }
}
