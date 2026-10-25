package com.epitech.fnah.model;

/**
 * Bruit fait par le joueur dans la régie (portes qui claquent, manivelle,
 * disjoncteur...). Les clowns l'entendent : plus il est fort, plus ils
 * avancent vite. Il retombe tout seul avec le temps.
 */
public class NoiseMeter {

    /** Bruit maximal. */
    public static final double MAX = 100.0;
    /** Bruit perdu par seconde. */
    public static final double DECAY_PER_SECOND = 12.0;
    /** Seuil à partir duquel le bruit attire franchement les clowns. */
    public static final double LOUD = 50.0;

    private double level;

    /** Crée un compteur silencieux. */
    public NoiseMeter() {
    }

    /**
     * Ajoute du bruit, sans dépasser {@value #MAX}.
     *
     * @param amount bruit ajouté
     * @throws IllegalArgumentException si la quantité est négative
     */
    public void make(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        level = Math.min(MAX, level + amount);
    }

    /**
     * Fait retomber le bruit.
     *
     * @param deltaSeconds temps écoulé, en secondes
     * @throws IllegalArgumentException si le temps est négatif
     */
    public void update(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        level = Math.max(0, level - DECAY_PER_SECOND * deltaSeconds);
    }

    /**
     * Renvoie le niveau de bruit.
     *
     * @return bruit entre 0 et {@value #MAX}
     */
    public double getLevel() {
        return level;
    }

    /**
     * Indique si le bruit attire franchement les clowns.
     *
     * @return {@code true} à partir de {@value #LOUD}
     */
    public boolean isLoud() {
        return level >= LOUD;
    }

    /**
     * Bonus de niveau d'IA donné aux clowns qui entendent ce bruit (0 à 4).
     *
     * @return bonus de niveau d'IA
     */
    public int hearingBonus() {
        return (int) (level / 25);
    }

    /** Remet le silence. */
    public void reset() {
        level = 0;
    }
}
