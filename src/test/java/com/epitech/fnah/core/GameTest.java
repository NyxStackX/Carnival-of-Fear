package com.epitech.fnah.core;

import com.epitech.fnah.config.CustomNight;
import com.epitech.fnah.config.NightConfig;
import com.epitech.fnah.entity.Bobo;
import com.epitech.fnah.entity.Henry;
import com.epitech.fnah.entity.Pepin;
import com.epitech.fnah.model.BarrelOrgan;
import com.epitech.fnah.model.CameraSystem;
import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.Room;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private static final double DT = 0.5;

    private static void run(Game game, double seconds) {
        for (double t = 0; t < seconds && game.getState() == GameState.PLAYING; t += DT) {
            game.tick(DT);
        }
    }

    private static void disableMimiAndZigEtZag(Game game) {
        game.getMimi().setBaseAiLevel(0);
        game.getZigEtZag().setBaseAiLevel(0);
    }

    @Test
    void startsInMenuOnNightOne() {
        Game game = new Game();
        assertEquals(GameState.MENU, game.getState());
        assertEquals(1, game.getCurrentNight());
    }

    @Test
    void startNightAppliesConfig() {
        Game game = new Game(() -> 20);
        game.startNight(3);
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(3, game.getCurrentNight());
        assertEquals(NightConfig.forNight(3).boboAiLevel(), game.getBobo().getBaseAiLevel());
    }

    @Test
    void doorToggleOnlyDuringNight() {
        Game game = new Game(() -> 20);
        game.toggleLeftDoor();
        assertFalse(game.getOffice().getLeftDoor().isClosed());
        game.startNight(1);
        game.toggleLeftDoor();
        assertTrue(game.getOffice().getLeftDoor().isClosed());
    }

    @Test
    void rightDoorToggleIsAvailableOnlyWithTabletDownDuringNight() {
        Game game = new Game(() -> 20);
        game.toggleRightDoor();
        assertFalse(game.getOffice().getRightDoor().isClosed());
        game.startNight(1);
        game.toggleTablet();
        game.toggleRightDoor();
        assertFalse(game.getOffice().getRightDoor().isClosed());
        game.toggleTablet();
        game.toggleRightDoor();
        assertTrue(game.getOffice().getRightDoor().isClosed());
    }

    @Test
    void pauseAndResumeAreOnlyAvailableDuringAnActiveNight() {
        Game game = new Game(() -> 20);
        game.pause();
        assertEquals(GameState.MENU, game.getState());

        game.startNight(1);
        game.pause();
        assertEquals(GameState.PAUSED, game.getState());
        game.resume();
        assertEquals(GameState.PLAYING, game.getState());
        game.resume();
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test
    void openDoorLetsBoboInAndEndsNight() {
        Game game = new Game(() -> 1);
        game.startNight(1);
        disableMimiAndZigEtZag(game);
        run(game, Bobo.ACTION_INTERVAL * 4);
        assertEquals(GameState.JUMPSCARE, game.getState());
        assertEquals("Bobo", game.getAttackerName());
        game.tick(GameLoop.JUMPSCARE_DURATION);
        assertEquals(GameState.GAME_OVER, game.getState());
    }

    @Test
    void doorClosedAllNightSurvivesUntilSix() {
        Game game = new Game(() -> 1);
        game.startNight(1);
        disableMimiAndZigEtZag(game);
        game.toggleLeftDoor();
        run(game, 400);
        assertEquals(GameState.WIN, game.getState());
        assertEquals(28.0, game.getPower().getEnergy(), 0.5);
    }

    @Test
    void closingDoorInTimeRepelsBobo() {
        Game game = new Game(() -> 1);
        game.startNight(1);
        disableMimiAndZigEtZag(game);
        run(game, Bobo.ACTION_INTERVAL * 3);
        assertEquals(Room.COULOIR_GAUCHE, game.getBobo().getRoom());
        game.toggleLeftDoor();
        run(game, Bobo.ACTION_INTERVAL);
        assertEquals(Bobo.RETREAT_ROOM, game.getBobo().getRoom());
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test
    void restartResetsEverything() {
        Game game = new Game(() -> 1);
        game.startNight(2);
        game.toggleLeftDoor();
        run(game, 30);
        game.restartNight();
        assertEquals(2, game.getCurrentNight());
        assertEquals(Bobo.START_ROOM, game.getBobo().getRoom());
        assertFalse(game.getOffice().getLeftDoor().isClosed());
        assertEquals("00:00", game.getClock().getFormattedTime());
        assertEquals(100.0, game.getPower().getEnergy());
        assertNull(game.getAttackerName());
    }

    @Test
    void nightsChainUntilTheLast() {
        Game game = new Game(() -> 20);
        game.startNight(1);
        assertTrue(game.hasNextNight());
        game.startNextNight();
        assertEquals(2, game.getCurrentNight());
        game.startNight(NightConfig.LAST_NIGHT);
        assertFalse(game.hasNextNight());
        assertThrows(IllegalStateException.class, game::startNextNight);
    }

    @Test
    void returnToMenuStopsTheNight() {
        Game game = new Game(() -> 20);
        game.startNight(1);
        game.returnToMenu();
        assertEquals(GameState.MENU, game.getState());
        game.tick(10.0);
        assertEquals(0.0, game.getClock().getElapsedSeconds());
    }

    @Test
    void selectNightDoesNotStartIt() {
        Game game = new Game(() -> 20);
        game.selectNight(4);
        assertEquals(4, game.getCurrentNight());
        assertEquals(GameState.MENU, game.getState());
        assertThrows(IllegalArgumentException.class, () -> game.selectNight(0));
    }

    @Test
    void publishesStateChanges() {
        Game game = new Game(() -> 20);
        List<GameState> seen = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.StateChanged.class, e -> seen.add(e.state()));
        game.startNight(1);
        game.returnToMenu();
        assertEquals(List.of(GameState.PLAYING, GameState.MENU), seen);
    }

    @Test
    void publishesBoboMoves() {
        Game game = new Game(() -> 1);
        List<GameEvent.AnimatronicMoved> moves = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.AnimatronicMoved.class, moves::add);
        game.startNight(1);
        disableMimiAndZigEtZag(game);
        run(game, Bobo.ACTION_INTERVAL);
        assertEquals(List.of(new GameEvent.AnimatronicMoved("Bobo", Room.PISTE_CENTRALE, Room.GRADINS)), moves);
    }

    @Test
    void publishesDoorToggles() {
        Game game = new Game(() -> 20);
        List<GameEvent.DoorToggled> toggles = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.DoorToggled.class, toggles::add);
        game.startNight(1);
        game.toggleLeftDoor();
        assertEquals(List.of(new GameEvent.DoorToggled(Door.Side.LEFT, true)), toggles);
    }

    @Test
    void publishesHourChanges() {
        Game game = new Game(() -> 20);
        List<Integer> hours = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.HourChanged.class, e -> hours.add(e.hour()));
        game.startNight(1);
        run(game, 125);
        assertEquals(List.of(1, 2), hours);
    }

    @Test
    void exposesTheCircusMap() {
        Game game = new Game(() -> 20);
        assertEquals(1, game.getMap().distance(Room.COULOIR_GAUCHE, Room.REGIE));
    }

    @Test
    void tabletOnlyDuringNight() {
        Game game = new Game(() -> 20);
        game.toggleTablet();
        assertFalse(game.getOffice().getCameras().isOpen());
        game.startNight(1);
        game.toggleTablet();
        assertTrue(game.getOffice().getCameras().isOpen());
    }

    @Test
    void doorUnreachableWhileTabletIsUp() {
        Game game = new Game(() -> 20);
        game.startNight(1);
        game.toggleTablet();
        game.toggleLeftDoor();
        assertFalse(game.getOffice().getLeftDoor().isClosed());
        game.toggleTablet();
        game.toggleLeftDoor();
        assertTrue(game.getOffice().getLeftDoor().isClosed());
    }

    @Test
    void camerasNeedTheTabletUp() {
        Game game = new Game(() -> 20);
        game.startNight(1);
        game.selectCamera(4);
        game.cycleCamera(1);
        assertEquals(CameraSystem.DEFAULT_CAMERA, game.getOffice().getCameras().getSelected());
        game.toggleTablet();
        game.selectCamera(4);
        assertEquals(Room.COULOIR_GAUCHE, game.getOffice().getCameras().getSelected());
        game.cycleCamera(1);
        assertEquals(Room.LOGES, game.getOffice().getCameras().getSelected());
    }

    @Test
    void unknownCameraNumberIsIgnored() {
        Game game = new Game(() -> 20);
        game.startNight(1);
        game.toggleTablet();
        game.selectCamera(9);
        assertEquals(CameraSystem.DEFAULT_CAMERA, game.getOffice().getCameras().getSelected());
    }

    @Test
    void tabletDrainsPower() {
        Game game = new Game(() -> 20);
        game.startNight(1);
        game.toggleTablet();
        game.tick(1.0);
        assertEquals(99.8, game.getPower().getEnergy(), 1e-9);
    }

    @Test
    void publishesTabletAndCameraEvents() {
        Game game = new Game(() -> 20);
        List<GameEvent> seen = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.TabletToggled.class, seen::add);
        game.getEvents().subscribe(GameEvent.CameraSelected.class, seen::add);
        game.startNight(1);
        game.toggleTablet();
        game.selectCamera(2);
        assertEquals(List.of(
                new GameEvent.TabletToggled(true, CameraSystem.DEFAULT_CAMERA),
                new GameEvent.CameraSelected(Room.GRADINS)), seen);
    }

    @Test
    void newNightLowersTablet() {
        Game game = new Game(() -> 20);
        game.startNight(1);
        game.toggleTablet();
        game.selectCamera(5);
        game.restartNight();
        assertFalse(game.getOffice().getCameras().isOpen());
        assertEquals(CameraSystem.DEFAULT_CAMERA, game.getOffice().getCameras().getSelected());
    }

    // ------------------------------------------------------------------ version finale

    private static Game quietGame(int night) {
        Game game = new Game(() -> 1);
        game.startNight(night);
        game.getBobo().setBaseAiLevel(0);
        disableMimiAndZigEtZag(game);
        game.getPepin().setBaseAiLevel(0);
        return game;
    }

    @Test
    void lightsOnlyWorkFromTheDesk() {
        Game game = quietGame(1);
        game.setLeftLight(true);
        game.setRightLight(true);
        assertTrue(game.getOffice().getLeftLight().isOn());
        assertTrue(game.getOffice().getRightLight().isOn());
        game.setLeftLight(false);
        assertFalse(game.getOffice().getLeftLight().isOn());

        game.toggleTablet();
        assertFalse(game.getOffice().getRightLight().isOn());
        game.setLeftLight(true);
        assertFalse(game.getOffice().getLeftLight().isOn());
    }

    @Test
    void lightsNeedANightInProgress() {
        Game game = new Game();
        game.setLeftLight(true);
        assertFalse(game.getOffice().getLeftLight().isOn());
    }

    @Test
    void lightToggleIsPublished() {
        Game game = quietGame(1);
        List<GameEvent.LightToggled> seen = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.LightToggled.class, seen::add);
        game.setRightLight(true);
        game.setRightLight(false);
        assertEquals(List.of(new GameEvent.LightToggled(Door.Side.RIGHT, true),
                new GameEvent.LightToggled(Door.Side.RIGHT, false)), seen);
    }

    @Test
    void crankOnlyWorksOnCameraEight() {
        Game game = quietGame(2);
        List<GameEvent.OrganWinding> seen = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.OrganWinding.class, seen::add);
        game.setWinding(true);
        assertFalse(game.getOffice().getOrgan().isWinding());
        game.toggleTablet();
        game.setWinding(true);
        assertFalse(game.getOffice().getOrgan().isWinding());
        game.selectCamera(Pepin.START_ROOM.getCameraNumber());
        game.setWinding(true);
        assertTrue(game.getOffice().getOrgan().isWinding());
        game.cycleCamera(1);
        assertFalse(game.getOffice().getOrgan().isWinding());
        assertEquals(List.of(new GameEvent.OrganWinding(true), new GameEvent.OrganWinding(false)), seen);
    }

    @Test
    void releaseControlsLetsGoOfEverything() {
        Game game = quietGame(2);
        game.setLeftLight(true);
        game.releaseControls();
        assertFalse(game.getOffice().getLeftLight().isOn());
        game.toggleTablet();
        game.selectCamera(8);
        game.setWinding(true);
        game.releaseControls();
        assertFalse(game.getOffice().getOrgan().isWinding());
    }

    @Test
    void neglectedOrganLetsPepinOut() {
        Game game = quietGame(2);
        game.getPepin().setBaseAiLevel(20);
        run(game, 120);
        assertEquals(GameState.JUMPSCARE, game.getState());
        assertEquals(Pepin.NAME, game.getAttackerName());
    }

    @Test
    void windingTheOrganKeepsPepinInside() {
        Game game = quietGame(2);
        game.getPepin().setBaseAiLevel(20);
        game.toggleTablet();
        game.selectCamera(8);
        game.setWinding(true);
        run(game, 120);
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(BarrelOrgan.MAX_CHARGE, game.getOffice().getOrgan().getCharge());
    }

    @Test
    void blackoutLocksTheControlsThenHenryAttacks() {
        Game game = quietGame(1);
        List<GameEvent> seen = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.PowerOut.class, seen::add);
        game.getEvents().subscribe(GameEvent.HenryMelody.class, seen::add);
        game.getPower().setEnergyMultiplier(500);
        game.tick(5);
        assertTrue(game.getPower().isPowerOut());
        assertEquals(GameState.PLAYING, game.getState());

        game.toggleLeftDoor();
        game.toggleRightDoor();
        game.toggleTablet();
        game.setLeftLight(true);
        assertFalse(game.getOffice().getLeftDoor().isClosed());
        assertFalse(game.getOffice().getRightDoor().isClosed());
        assertFalse(game.getOffice().getCameras().isOpen());
        assertFalse(game.getOffice().getLeftLight().isOn());

        run(game, Henry.DARK_BASE + Henry.SECONDS_PER_ROLL + 1);
        assertTrue(game.getHenry().isPlayingMelody());
        assertTrue(game.isThreatNear());
        run(game, Henry.MELODY_BASE + Henry.SECONDS_PER_ROLL + 1);
        assertEquals(GameState.JUMPSCARE, game.getState());
        assertEquals(Henry.NAME, game.getAttackerName());
        assertEquals(List.of(new GameEvent.PowerOut(), new GameEvent.HenryMelody(true),
                new GameEvent.HenryMelody(false)), seen);
    }

    @Test
    void sixAmSavesYouFromHenry() {
        Game game = quietGame(1);
        game.tick(GameClock.SECONDS_PER_HOUR * 6 - 3);
        game.getPower().setEnergyMultiplier(10_000);
        game.tick(1);
        assertTrue(game.getPower().isPowerOut());
        run(game, 10);
        assertEquals(GameState.WIN, game.getState());
    }

    @Test
    void threatNearFollowsTheClowns() {
        Game game = new Game(() -> 1);
        game.startNight(5);
        disableMimiAndZigEtZag(game);
        game.getPepin().setBaseAiLevel(0);
        assertFalse(game.isThreatNear());
        run(game, Bobo.ACTION_INTERVAL * 3);
        assertTrue(game.getBobo().isAtDoor());
        assertTrue(game.isThreatNear());
    }

    @Test
    void customNightUsesChosenLevels() {
        Game game = new Game(() -> 20);
        List<GameEvent.NightStarted> seen = new ArrayList<>();
        game.getEvents().subscribe(GameEvent.NightStarted.class, seen::add);
        game.startCustomNight(new CustomNight(1, 2, 3, 4));
        assertTrue(game.isCustomNight());
        assertTrue(game.hasHallucinations());
        assertFalse(game.hasNextNight());
        assertEquals(1, game.getBobo().getBaseAiLevel());
        assertEquals(2, game.getMimi().getBaseAiLevel());
        assertEquals(3, game.getZigEtZag().getBaseAiLevel());
        assertEquals(4, game.getPepin().getBaseAiLevel());
        assertEquals(List.of(new GameEvent.NightStarted(NightConfig.LAST_NIGHT, true, false)), seen);

        game.getBobo().setBaseAiLevel(15);
        game.restartNight();
        assertTrue(game.isCustomNight());
        assertEquals(1, game.getBobo().getBaseAiLevel());

        game.startNight(1);
        assertFalse(game.isCustomNight());
        assertEquals(0, game.getPepin().getBaseAiLevel());
        assertThrows(NullPointerException.class, () -> game.startCustomNight(null));
    }

    @Test
    void hallucinationsFollowTheNight() {
        Game game = new Game(() -> 1);
        game.startNight(2);
        assertFalse(game.hasHallucinations());
        game.startNight(3);
        assertTrue(game.hasHallucinations());
    }

    @Test
    void endingOnlyAfterWinningNightFive() {
        Game game = quietGame(4);
        assertFalse(game.canPlayEnding());
        assertThrows(IllegalStateException.class, game::playEnding);
        game.tick(400);
        assertEquals(GameState.WIN, game.getState());
        assertFalse(game.canPlayEnding());

        Game last = quietGame(NightConfig.LAST_NIGHT);
        last.tick(400);
        assertTrue(last.canPlayEnding());
        last.playEnding();
        assertEquals(GameState.ENDING, last.getState());
        last.returnToMenu();
        assertEquals(GameState.MENU, last.getState());
    }

    @Test
    void customNightHasNoEnding() {
        Game game = new Game(() -> 1);
        game.startCustomNight(new CustomNight(0, 0, 0, 0));
        game.tick(400);
        assertEquals(GameState.WIN, game.getState());
        assertFalse(game.canPlayEnding());
    }
}
