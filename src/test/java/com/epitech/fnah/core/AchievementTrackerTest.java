package com.epitech.fnah.core;

import com.epitech.fnah.config.Achievement;
import com.epitech.fnah.config.CustomNight;
import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.config.NightConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AchievementTrackerTest {

    private Game game;
    private GameConfig config;
    private List<Achievement> announced;

    @BeforeEach
    void setUp() {
        game = new Game(() -> 20);
        config = new GameConfig(Path.of("unused.properties"));
        announced = new ArrayList<>();
        new AchievementTracker(game.getEvents(), config);
        game.getEvents().subscribe(GameEvent.AchievementUnlocked.class, e -> announced.add(e.achievement()));
    }

    /** Une nuit sans aucun clown, pour tester les étoiles sans se faire attaquer. */
    private void quietNight(int night) {
        game.startNight(night);
        game.getBobo().setBaseAiLevel(0);
        game.getMimi().setBaseAiLevel(0);
        game.getZigEtZag().setBaseAiLevel(0);
        game.getPepin().setBaseAiLevel(0);
    }

    @Test
    void quietNightWithoutDoorsOrCamerasGivesBothStars() {
        quietNight(1);
        game.tick(400);
        assertEquals(GameState.WIN, game.getState());
        assertEquals(Set.of(Achievement.OPEN_DOORS, Achievement.EYES_CLOSED), config.getAchievements());
        assertEquals(List.of(Achievement.OPEN_DOORS, Achievement.EYES_CLOSED), announced);
    }

    @Test
    void usingDoorsAndCamerasGivesNothingBeforeNightFive() {
        quietNight(2);
        game.toggleLeftDoor();
        game.toggleTablet();
        game.tick(400);
        assertEquals(GameState.WIN, game.getState());
        assertTrue(config.getAchievements().isEmpty());
    }

    @Test
    void winningNightFiveGivesTheLastShow() {
        quietNight(NightConfig.LAST_NIGHT);
        game.toggleLeftDoor();
        game.toggleTablet();
        game.tick(400);
        assertEquals(Set.of(Achievement.LAST_SHOW), config.getAchievements());
    }

    @Test
    void statsResetAtEachNight() {
        quietNight(1);
        game.toggleLeftDoor();
        game.restartNight();
        game.getBobo().setBaseAiLevel(0);
        game.getMimi().setBaseAiLevel(0);
        game.getZigEtZag().setBaseAiLevel(0);
        game.tick(400);
        assertTrue(config.isUnlocked(Achievement.OPEN_DOORS));
    }

    @Test
    void maxCustomNightGivesTheChallengeStar() {
        game.startCustomNight(CustomNight.maxChallenge());
        game.getEvents().publish(new GameEvent.StateChanged(GameState.WIN));
        assertTrue(config.isUnlocked(Achievement.MAX_CHALLENGE));
        assertFalse(config.isUnlocked(Achievement.LAST_SHOW));
    }

    @Test
    void easyCustomNightDoesNotGiveTheChallengeStar() {
        game.startCustomNight(new CustomNight(0, 0, 0, 0));
        game.toggleLeftDoor();
        game.toggleTablet();
        game.tick(400);
        assertEquals(GameState.WIN, game.getState());
        assertTrue(config.getAchievements().isEmpty());
    }

    @Test
    void starsAreAnnouncedOnlyOnce() {
        quietNight(1);
        game.tick(400);
        quietNight(1);
        game.tick(400);
        assertEquals(2, announced.size());
    }
}
