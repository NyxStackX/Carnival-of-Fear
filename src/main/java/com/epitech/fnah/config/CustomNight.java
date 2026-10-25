package com.epitech.fnah.config;

/**
 * Réglages de la nuit personnalisée, débloquée après la nuit 5 : le joueur
 * choisit le niveau de chaque clown, de 0 (absent) à {@value #MAX_LEVEL}.
 *
 * @param bobo     niveau de Bobo
 * @param mimi     niveau de Mimi
 * @param zigEtZag niveau de Zig et Zag
 * @param pepin    niveau de Pépin
 */
public record CustomNight(int bobo, int mimi, int zigEtZag, int pepin) {

    /** Niveau maximal d'un clown. */
    public static final int MAX_LEVEL = 20;
    /** Multiplicateur de consommation d'énergie de la nuit personnalisée. */
    public static final double ENERGY_MULTIPLIER = 1.2;
    /** Nombre de clowns réglables. */
    public static final int CLOWN_COUNT = 4;

    /** Vérifie que chaque niveau est entre 0 et {@value #MAX_LEVEL}. */
    public CustomNight {
        for (int level : new int[]{bobo, mimi, zigEtZag, pepin}) {
            if (level < 0 || level > MAX_LEVEL) {
                throw new IllegalArgumentException("level must be between 0 and " + MAX_LEVEL);
            }
        }
    }

    /**
     * Réglage proposé par défaut : tous les clowns au niveau 5.
     *
     * @return réglage par défaut
     */
    public static CustomNight standard() {
        return new CustomNight(5, 5, 5, 5);
    }

    /**
     * Le défi maximal : tous les clowns au niveau {@value #MAX_LEVEL}.
     *
     * @return réglage du défi maximal
     */
    public static CustomNight maxChallenge() {
        return new CustomNight(MAX_LEVEL, MAX_LEVEL, MAX_LEVEL, MAX_LEVEL);
    }

    /**
     * Indique si tous les clowns sont au niveau maximal.
     *
     * @return {@code true} pour le défi 20 / 20 / 20 / 20
     */
    public boolean isMaxChallenge() {
        return equals(maxChallenge());
    }

    /**
     * Renvoie le niveau d'un clown par son rang (0 Bobo, 1 Mimi, 2 Zig et Zag, 3 Pépin).
     *
     * @param index rang du clown
     * @return son niveau
     * @throws IllegalArgumentException si le rang n'existe pas
     */
    public int level(int index) {
        return switch (index) {
            case 0 -> bobo;
            case 1 -> mimi;
            case 2 -> zigEtZag;
            case 3 -> pepin;
            default -> throw new IllegalArgumentException("unknown clown index " + index);
        };
    }

    /**
     * Renvoie une copie où le niveau d'un clown a changé, en restant entre 0 et
     * {@value #MAX_LEVEL} (au-delà, on repart de l'autre bout).
     *
     * @param index rang du clown
     * @param step  variation (+1, -1...)
     * @return nouveau réglage
     */
    public CustomNight adjust(int index, int step) {
        int[] levels = {bobo, mimi, zigEtZag, pepin};
        level(index);
        levels[index] = Math.floorMod(levels[index] + step, MAX_LEVEL + 1);
        return new CustomNight(levels[0], levels[1], levels[2], levels[3]);
    }
}
