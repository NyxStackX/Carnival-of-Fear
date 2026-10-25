package com.epitech.fnah.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;

/**
 * Un écran du jeu (menu, nuit, jumpscare, fin de nuit).
 *
 * <p>Patron State : {@link ScreenManager} choisit l'écran actif selon l'état
 * du jeu et lui transmet le temps, le dessin et les entrées. Chaque écran ne
 * s'occupe que de lui-même.</p>
 */
interface Screen {

    /** Appelé quand l'écran devient actif. */
    default void onEnter() {
    }

    /**
     * Fait avancer les animations de l'écran.
     *
     * @param dt temps écoulé en secondes
     */
    void update(double dt);

    /**
     * Dessine l'écran.
     *
     * @param gc contexte graphique du canvas
     */
    void render(GraphicsContext gc);

    /**
     * Réagit à une touche.
     *
     * @param event touche pressée
     */
    default void onKey(KeyEvent event) {
    }

    /**
     * Réagit au relâchement d'une touche (commandes maintenues : lampes, manivelle).
     *
     * @param event touche relâchée
     */
    default void onKeyReleased(KeyEvent event) {
    }

    /**
     * Réagit à l'appui sur le bouton de la souris (avant le clic).
     *
     * @param x abscisse
     * @param y ordonnée
     */
    default void onMousePressed(double x, double y) {
    }

    /**
     * Réagit au relâchement du bouton de la souris.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    default void onMouseReleased(double x, double y) {
    }

    /**
     * Réagit à un clic.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    default void onClick(double x, double y) {
    }

    /**
     * Réagit au déplacement de la souris.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    default void onMouseMove(double x, double y) {
    }
}
