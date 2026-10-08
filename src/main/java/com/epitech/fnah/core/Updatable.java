package com.epitech.fnah.core;

/**
 * Élément mis à jour à chaque tick de la boucle de jeu (clowns, mécaniques...).
 */
public interface Updatable {

    /**
     * Met à jour l'élément pour ce tick.
     *
     * @param deltaSeconds temps écoulé depuis le tick précédent, en secondes
     * @param ctx          état de la nuit en cours
     */
    void update(double deltaSeconds, GameContext ctx);

    /** Remet l'élément dans son état de début de nuit. Ne fait rien par défaut. */
    default void reset() {
    }
}
