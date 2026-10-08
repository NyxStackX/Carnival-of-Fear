package com.epitech.fnah.core;

import com.epitech.fnah.model.PowerSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GameLoopTest {

    private GameClock clock;
    private PowerSystem power;
    private GameLoop loop;

    @BeforeEach
    void setUp() {
        clock = new GameClock();
        power = new PowerSystem();
        loop = new GameLoop(clock, power);
    }

    @Test
    void startsInMenuState() {
        assertEquals(GameState.MENU, loop.getState());
    }

    @Test
    void tickInMenuDoesNothing() {
        loop.tick(1.0);
        assertEquals(0.0, clock.getElapsedSeconds());
    }

    @Test
    void startNightSwitchesToPlayingAndResets() {
        clock.update(100);
        power.update(100);
        loop.startNight();
        assertEquals(GameState.PLAYING, loop.getState());
        assertEquals(0.0, clock.getElapsedSeconds());
        assertEquals(100.0, power.getEnergy());
    }

    @Test
    void tickAdvancesClockAndPower() {
        loop.startNight();
        loop.tick(10.0);
        assertEquals(10.0, clock.getElapsedSeconds());
        assertTrue(power.getEnergy() < 100.0);
    }

    @Test
    void tickRejectsNegativeDelta() {
        loop.startNight();
        assertThrows(IllegalArgumentException.class, () -> loop.tick(-1.0));
    }

    @Test
    void reachingSixAMWinsTheNight() {
        loop.startNight();
        loop.tick(360.0);
        assertEquals(GameState.WIN, loop.getState());
    }

    @Test
    void powerOutEndsTheGame() {
        loop.startNight();
        loop.tick(10_000.0);
        loop.tick(0.1);
        assertEquals(GameState.GAME_OVER, loop.getState());
    }

    @Test
    void registeredSystemsAreUpdated() {
        AtomicInteger updates = new AtomicInteger();
        loop.registerSystem((dt, ctx) -> updates.incrementAndGet());
        loop.startNight();
        loop.tick(1.0);
        loop.tick(1.0);
        assertEquals(2, updates.get());
    }

    @Test
    void systemsNotUpdatedOutsidePlaying() {
        AtomicInteger updates = new AtomicInteger();
        loop.registerSystem((dt, ctx) -> updates.incrementAndGet());
        loop.tick(1.0); // still MENU
        assertEquals(0, updates.get());
    }

    @Test
    void stateChangeListenersFire() {
        List<GameState> seen = new ArrayList<>();
        loop.onStateChange(seen::add);
        loop.startNight();
        loop.tick(360.0);
        assertEquals(List.of(GameState.PLAYING, GameState.WIN), seen);
    }

    @Test
    void contextExposesClockAndPower() {
        assertSame(clock, loop.getContext().clock);
        assertSame(power, loop.getContext().power);
    }

    @Test
    void attackStartsJumpscareThenGameOver() {
        loop.registerSystem((dt, ctx) -> ctx.attackPlayer("Bobo"));
        loop.startNight();
        loop.tick(0.1);
        assertEquals(GameState.JUMPSCARE, loop.getState());
        assertEquals("Bobo", loop.getAttackerName());
        loop.tick(GameLoop.JUMPSCARE_DURATION - 0.1);
        assertEquals(GameState.JUMPSCARE, loop.getState());
        loop.tick(0.1);
        assertEquals(GameState.GAME_OVER, loop.getState());
    }

    @Test
    void jumpscareFreezesTheClock() {
        loop.registerSystem((dt, ctx) -> ctx.attackPlayer("Bobo"));
        loop.startNight();
        loop.tick(1.0);
        double elapsed = clock.getElapsedSeconds();
        loop.tick(1.0);
        assertEquals(elapsed, clock.getElapsedSeconds());
    }

    @Test
    void attackIgnoredOutsideNight() {
        loop.triggerJumpscare("Bobo");
        assertEquals(GameState.MENU, loop.getState());
        assertNull(loop.getAttackerName());
    }

    @Test
    void sixAmWinsBeforeALateAttack() {
        loop.registerSystem((dt, ctx) -> {
            if (ctx.clock.isNightOver()) ctx.attackPlayer("Bobo");
        });
        loop.startNight();
        loop.tick(360.0);
        assertEquals(GameState.WIN, loop.getState());
    }

    @Test
    void startNightResetsSystemsAndOffice() {
        AtomicInteger resets = new AtomicInteger();
        loop.registerSystem(new Updatable() {
            @Override
            public void update(double dt, GameContext ctx) {
            }

            @Override
            public void reset() {
                resets.incrementAndGet();
            }
        });
        loop.getContext().office.getLeftDoor().toggle();
        loop.startNight();
        assertEquals(1, resets.get());
        assertFalse(loop.getContext().office.getLeftDoor().isClosed());
    }

    @Test
    void closedDoorDrainsMorePower() {
        loop.startNight();
        loop.getContext().office.getLeftDoor().toggle();
        loop.tick(1.0);
        assertEquals(99.8, power.getEnergy(), 1e-9);
    }
}