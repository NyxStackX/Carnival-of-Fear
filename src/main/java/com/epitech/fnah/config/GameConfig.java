package com.epitech.fnah.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Progression du joueur, enregistrée dans un fichier {@code config.properties}.
 * Pour l'instant : la dernière nuit débloquée.
 */
public class GameConfig {

    /** Clé de la nuit débloquée dans le fichier. */
    static final String KEY_UNLOCKED_NIGHT = "unlockedNight";

    private final Path file;
    private int unlockedNight = 1;

    /**
     * Crée une configuration liée à un fichier, avec les valeurs par défaut.
     *
     * @param file fichier de sauvegarde (créé au premier enregistrement)
     */
    public GameConfig(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    /**
     * Emplacement par défaut : {@code ~/.cirque-malvaux/config.properties}.
     *
     * @return une configuration pointant vers ce fichier
     */
    public static GameConfig inUserHome() {
        return new GameConfig(Path.of(System.getProperty("user.home"), ".cirque-malvaux", "config.properties"));
    }

    /**
     * Lit le fichier. S'il n'existe pas, garde les valeurs par défaut.
     * Une valeur absente ou invalide est remplacée par la valeur par défaut.
     *
     * @throws IOException si le fichier existe mais ne peut pas être lu
     */
    public void load() throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        unlockedNight = parseNight(props.getProperty(KEY_UNLOCKED_NIGHT));
    }

    /**
     * Écrit le fichier, en créant le dossier si besoin.
     *
     * @throws IOException si l'écriture échoue
     */
    public void save() throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Properties props = new Properties();
        props.setProperty(KEY_UNLOCKED_NIGHT, Integer.toString(unlockedNight));
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            props.store(writer, "Minuit au Cirque Malvaux");
        }
    }

    /**
     * Renvoie la dernière nuit débloquée, entre 1 et {@link NightConfig#LAST_NIGHT}.
     *
     * @return dernière nuit débloquée, entre 1 et {@link NightConfig#LAST_NIGHT}
     */
    public int getUnlockedNight() {
        return unlockedNight;
    }

    /**
     * Débloque une nuit. Ne fait jamais reculer la progression.
     *
     * @param night nuit à débloquer
     * @throws IllegalArgumentException si la nuit n'existe pas
     */
    public void unlockNight(int night) {
        NightConfig.forNight(night);
        unlockedNight = Math.max(unlockedNight, night);
    }

    /** Remet la progression à la nuit 1. */
    public void resetProgress() {
        unlockedNight = 1;
    }

    /**
     * Renvoie le chemin du fichier de sauvegarde.
     *
     * @return chemin du fichier de sauvegarde
     */
    public Path getFile() {
        return file;
    }

    private static int parseNight(String value) {
        if (value == null) {
            return 1;
        }
        try {
            int night = Integer.parseInt(value.trim());
            return Math.clamp(night, 1, NightConfig.LAST_NIGHT);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
