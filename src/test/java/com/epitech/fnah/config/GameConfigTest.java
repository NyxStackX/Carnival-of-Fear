package com.epitech.fnah.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameConfigTest {

    private Path dir;
    private Path file;

    @BeforeEach
    void setUp() throws IOException {
        dir = Files.createTempDirectory("cirque-config");
        file = dir.resolve("sub").resolve("config.properties");
    }

    @Test
    void startsAtNightOne() {
        assertEquals(1, new GameConfig(file).getUnlockedNight());
    }

    @Test
    void missingFileKeepsDefaults() throws IOException {
        GameConfig config = new GameConfig(file);
        config.load();
        assertEquals(1, config.getUnlockedNight());
    }

    @Test
    void saveThenLoadRestoresProgress() throws IOException {
        GameConfig config = new GameConfig(file);
        config.unlockNight(3);
        config.save();
        assertTrue(Files.exists(file));

        GameConfig reloaded = new GameConfig(file);
        reloaded.load();
        assertEquals(3, reloaded.getUnlockedNight());
    }

    @Test
    void unlockingNeverGoesBackwards() {
        GameConfig config = new GameConfig(file);
        config.unlockNight(4);
        config.unlockNight(2);
        assertEquals(4, config.getUnlockedNight());
    }

    @Test
    void rejectsUnknownNight() {
        GameConfig config = new GameConfig(file);
        assertThrows(IllegalArgumentException.class, () -> config.unlockNight(NightConfig.LAST_NIGHT + 1));
    }

    @Test
    void resetProgressGoesBackToNightOne() {
        GameConfig config = new GameConfig(file);
        config.unlockNight(5);
        config.resetProgress();
        assertEquals(1, config.getUnlockedNight());
    }

    @Test
    void invalidValueFallsBackToNightOne() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, GameConfig.KEY_UNLOCKED_NIGHT + "=abc\n");
        GameConfig config = new GameConfig(file);
        config.load();
        assertEquals(1, config.getUnlockedNight());
    }

    @Test
    void outOfRangeValueIsClamped() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, GameConfig.KEY_UNLOCKED_NIGHT + "=99\n");
        GameConfig config = new GameConfig(file);
        config.load();
        assertEquals(NightConfig.LAST_NIGHT, config.getUnlockedNight());
    }

    @Test
    void missingKeyFallsBackToNightOne() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "other=1\n");
        GameConfig config = new GameConfig(file);
        config.load();
        assertEquals(1, config.getUnlockedNight());
    }

    @Test
    void defaultLocationIsInUserHome() {
        Path path = GameConfig.inUserHome().getFile();
        assertTrue(path.startsWith(Path.of(System.getProperty("user.home"))));
        assertEquals("config.properties", path.getFileName().toString());
    }

    @Test
    void achievementsAreSavedAndLoaded() throws IOException {
        GameConfig config = new GameConfig(file);
        assertTrue(config.unlock(Achievement.OPEN_DOORS));
        assertFalse(config.unlock(Achievement.OPEN_DOORS));
        config.unlock(Achievement.LAST_SHOW);
        config.save();

        GameConfig loaded = new GameConfig(file);
        loaded.load();
        assertTrue(loaded.isUnlocked(Achievement.OPEN_DOORS));
        assertTrue(loaded.isUnlocked(Achievement.LAST_SHOW));
        assertFalse(loaded.isUnlocked(Achievement.EYES_CLOSED));
        assertEquals(2, loaded.getAchievements().size());
    }

    @Test
    void unknownAchievementsAreIgnored() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "achievements=NOPE, EYES_CLOSED ,,\n");
        GameConfig config = new GameConfig(file);
        config.load();
        assertEquals(Set.of(Achievement.EYES_CLOSED), config.getAchievements());
    }

    @Test
    void customNightUnlocksWithTheLastShow() {
        GameConfig config = new GameConfig(file);
        assertFalse(config.isCustomNightUnlocked());
        config.unlock(Achievement.LAST_SHOW);
        assertTrue(config.isCustomNightUnlocked());
    }

    @Test
    void resetClearsAchievements() {
        GameConfig config = new GameConfig(file);
        config.unlock(Achievement.MAX_CHALLENGE);
        config.resetProgress();
        assertTrue(config.getAchievements().isEmpty());
    }

    @Test
    void achievementsViewIsReadOnly() {
        GameConfig config = new GameConfig(file);
        assertThrows(UnsupportedOperationException.class,
                () -> config.getAchievements().add(Achievement.LAST_SHOW));
        assertThrows(NullPointerException.class, () -> config.unlock(null));
    }

    @Test
    void everyAchievementHasATitleAndDescription() {
        for (Achievement achievement : Achievement.values()) {
            assertFalse(achievement.getTitle().isBlank());
            assertFalse(achievement.getDescription().isBlank());
        }
    }

    @Test
    void volumeIsClampedSavedAndLoaded() throws IOException {
        GameConfig config = new GameConfig(file);
        assertEquals(GameConfig.DEFAULT_VOLUME, config.getVolume());
        config.adjustVolume(1);
        assertEquals(GameConfig.DEFAULT_VOLUME + GameConfig.VOLUME_STEP, config.getVolume());
        config.setVolume(150);
        assertEquals(100, config.getVolume());
        config.setVolume(-5);
        assertEquals(0, config.getVolume());
        config.setVolume(30);
        config.save();
        GameConfig loaded = new GameConfig(file);
        loaded.load();
        assertEquals(30, loaded.getVolume());
    }

    @Test
    void invalidVolumeFallsBackToDefault() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "volume=fort\n");
        GameConfig config = new GameConfig(file);
        config.load();
        assertEquals(GameConfig.DEFAULT_VOLUME, config.getVolume());
    }
}
