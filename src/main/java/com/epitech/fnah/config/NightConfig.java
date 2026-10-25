package com.epitech.fnah.config;

/**
 * Réglages d'une nuit. Les valeurs de Bobo suivent le GDD (section 7) ;
 * Mimi et Zig et Zag gagnent progressivement en activité.
 *
 * @param number           numéro de la nuit (1 à {@value #LAST_NIGHT})
 * @param boboAiLevel      niveau d'IA de départ de Bobo
 * @param energyMultiplier multiplicateur de consommation d'énergie
 */
public record NightConfig(int number, int boboAiLevel, double energyMultiplier) {

    /** Dernière nuit jouable. */
    public static final int LAST_NIGHT = 5;

    private static final NightConfig[] NIGHTS = {
            new NightConfig(1, 3, 1.0),
            new NightConfig(2, 5, 1.0),
            new NightConfig(3, 7, 1.1),
            new NightConfig(4, 10, 1.1),
            new NightConfig(5, 13, 1.2),
    };

    /** Vérifie les valeurs à la création. */
    public NightConfig {
        if (number < 1) {
            throw new IllegalArgumentException("number must be >= 1");
        }
        if (energyMultiplier <= 0) {
            throw new IllegalArgumentException("energyMultiplier must be > 0");
        }
    }

    /**
     * Donne les réglages d'une nuit.
     *
     * @param night numéro de la nuit
     * @return les réglages de cette nuit
     * @throws IllegalArgumentException si la nuit n'existe pas
     */
    public static NightConfig forNight(int night) {
        if (night < 1 || night > LAST_NIGHT) {
            throw new IllegalArgumentException("night must be between 1 and " + LAST_NIGHT);
        }
        return NIGHTS[night - 1];
    }

    /**
     * Niveau d'IA de départ de Mimi pour cette nuit.
     *
     * @return niveau d'IA de Mimi
     */
    public int mimiAiLevel() {
        return switch (number) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 8;
            default -> 12;
        };
    }

    /**
     * Niveau d'IA de départ de Pépin pour cette nuit : sa boîte à musique se
     * vide d'autant plus vite. Il dort pendant la nuit 1.
     *
     * @return niveau d'IA de Pépin
     */
    public int pepinAiLevel() {
        return switch (number) {
            case 1 -> 0;
            case 2 -> 2;
            case 3 -> 4;
            case 4 -> 6;
            default -> 9;
        };
    }

    /**
     * Indique si le veilleur commence à avoir des hallucinations (à partir de la nuit 3).
     *
     * @return {@code true} à partir de la nuit 3
     */
    public boolean hasHallucinations() {
        return number >= 3;
    }

    /**
     * Niveau d'IA de départ de Zig et Zag pour cette nuit.
     *
     * @return niveau d'IA de Zig et Zag
     */
    public int zigEtZagAiLevel() {
        return switch (number) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 6;
            case 4 -> 9;
            default -> 13;
        };
    }
}
