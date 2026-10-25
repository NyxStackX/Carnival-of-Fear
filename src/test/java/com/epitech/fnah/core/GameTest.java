package com.epitech.fnah.core;

import com.epitech.fnah.config.NightConfig;
import com.epitech.fnah.entity.Bobo;
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
}
