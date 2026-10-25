package com.epitech.fnah.core;

import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
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
    void pausedNightDoesNotAdvanceUntilResumed() {
        AtomicInteger updates = new AtomicInteger();
        loop.registerSystem((dt, ctx) -> updates.incrementAndGet());
        loop.startNight();
        loop.tick(1.0);
        double elapsed = clock.getElapsedSeconds();
        double energy = power.getEnergy();
        int count = updates.get();

        loop.pause();
        loop.tick(10.0);
        assertEquals(GameState.PAUSED, loop.getState());
        assertEquals(elapsed, clock.getElapsedSeconds());
        assertEquals(energy, power.getEnergy());
        assertEquals(count, updates.get());

        loop.resume();
        loop.tick(1.0);
        assertEquals(GameState.PLAYING, loop.getState());
        assertEquals(elapsed + 1.0, clock.getElapsedSeconds());
        assertEquals(count + 1, updates.get());
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
    void powerOutStartsBlackoutInsteadOfEndingTheNight() {
        List<GameEvent> events = new ArrayList<>();
        GameLoop dark = new GameLoop(clock, power, new Office(), MapGraph.circus(), events::add);
        dark.startNight();
        power.setEnergyMultiplier(100);
        dark.tick(11.0);
        assertTrue(power.isPowerOut());
        assertEquals(GameState.PLAYING, dark.getState());
        assertTrue(events.contains(new GameEvent.PowerOut()));
    }

    @Test
    void blackoutStillWinsAtSix() {
        loop.startNight();
        power.setEnergyMultiplier(100);
        loop.tick(11.0);
        loop.tick(360.0);
        assertEquals(GameState.WIN, loop.getState());
    }

    @Test
    void powerOutOpensDoorsAndLowersTablet() {
        Office office = new Office();
        GameLoop dark = new GameLoop(clock, power, office, MapGraph.circus());
        dark.startNight();
        office.getLeftDoor().toggle();
        office.getRightDoor().toggle();
        office.getRightDoor().setBlocked(true);
        office.getLeftLight().setOn(true);
        office.getCameras().toggleTablet();
        power.setEnergyMultiplier(100);
        dark.tick(5.0);
        assertTrue(power.isPowerOut());
        assertFalse(office.getLeftDoor().isClosed());
        assertFalse(office.getRightDoor().isClosed());
        assertFalse(office.getRightDoor().isBlocked());
        assertFalse(office.getLeftLight().isOn());
        assertFalse(office.getCameras().isOpen());
    }

    @Test
    void onlyBlackoutSystemsRunInTheDark() {
        AtomicInteger normal = new AtomicInteger();
        AtomicInteger dark = new AtomicInteger();
        loop.registerSystem((dt, ctx) -> normal.incrementAndGet());
        loop.registerBlackoutSystem((dt, ctx) -> dark.incrementAndGet());
        loop.startNight();
        loop.tick(1.0);
        assertEquals(1, normal.get());
        assertEquals(0, dark.get());
        power.setEnergyMultiplier(1000);
        loop.tick(1.0);
        loop.tick(1.0);
        assertEquals(1, normal.get());
        assertEquals(2, dark.get());
    }

    @Test
    void blackoutSystemsAreResetWithTheNight() {
        AtomicInteger resets = new AtomicInteger();
        loop.registerBlackoutSystem(new Updatable() {
            @Override
            public void update(double deltaSeconds, GameContext ctx) {
            }

            @Override
            public void reset() {
                resets.incrementAndGet();
            }
        });
        loop.startNight();
        loop.startNight();
        assertEquals(2, resets.get());
    }

    @Test
    void blackoutAttackStillLeadsToGameOver() {
        loop.registerBlackoutSystem((dt, ctx) -> ctx.attackPlayer("Henry"));
        loop.startNight();
        power.setEnergyMultiplier(1000);
        loop.tick(1.0);
        loop.tick(0.1);
        assertEquals(GameState.JUMPSCARE, loop.getState());
        assertEquals("Henry", loop.getAttackerName());
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