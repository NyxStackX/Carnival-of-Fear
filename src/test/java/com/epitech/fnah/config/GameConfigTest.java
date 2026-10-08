package com.epitech.fnah.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
