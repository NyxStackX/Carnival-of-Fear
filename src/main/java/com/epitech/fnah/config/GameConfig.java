package com.epitech.fnah.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Properties;

/**
 * Progression du joueur, enregistrée dans un fichier {@code config.properties}.
 * Contient la dernière nuit débloquée et les étoiles obtenues.
 */
public class GameConfig {

    /** Clé de la nuit débloquée dans le fichier. */
    static final String KEY_UNLOCKED_NIGHT = "unlockedNight";
    /** Clé des étoiles obtenues dans le fichier. */
    static final String KEY_ACHIEVEMENTS = "achievements";

    private final Path file;
    private int unlockedNight = 1;
    private final Set<Achievement> achievements = EnumSet.noneOf(Achievement.class);

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
        achievements.clear();
        achievements.addAll(parseAchievements(props.getProperty(KEY_ACHIEVEMENTS)));
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
        props.setProperty(KEY_ACHIEVEMENTS, achievements.stream().map(Enum::name).collect(Collectors.joining(",")));
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

    /** Remet la progression à zéro : nuit 1, aucune étoile. */
    public void resetProgress() {
        unlockedNight = 1;
        achievements.clear();
    }

    /**
     * Ajoute une étoile.
     *
     * @param achievement étoile obtenue
     * @return {@code true} si elle n'était pas encore obtenue
     */
    public boolean unlock(Achievement achievement) {
        return achievements.add(Objects.requireNonNull(achievement, "achievement"));
    }

    /**
     * Indique si une étoile est obtenue.
     *
     * @param achievement étoile
     * @return {@code true} si elle est obtenue
     */
    public boolean isUnlocked(Achievement achievement) {
        return achievements.contains(achievement);
    }

    /**
     * Renvoie les étoiles obtenues, en lecture seule.
     *
     * @return étoiles obtenues
     */
    public Set<Achievement> getAchievements() {
        return Collections.unmodifiableSet(achievements);
    }

    /**
     * Indique si la nuit personnalisée est accessible (nuit 5 terminée).
     *
     * @return {@code true} si la nuit 5 a été terminée
     */
    public boolean isCustomNightUnlocked() {
        return isUnlocked(Achievement.LAST_SHOW);
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

    private static Set<Achievement> parseAchievements(String value) {
        Set<Achievement> result = EnumSet.noneOf(Achievement.class);
        if (value == null) {
            return result;
        }
        for (String name : value.split(",")) {
            for (Achievement achievement : Achievement.values()) {
                if (achievement.name().equals(name.trim())) {
                    result.add(achievement);
                }
            }
        }
        return result;
    }
}
