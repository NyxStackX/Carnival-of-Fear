package com.epitech.fnah.core;

/**
 * États du jeu. L'affichage choisit l'écran à dessiner selon l'état courant.
 */
public enum GameState {
    /** Menu principal. */
    MENU,
    /** Écran « 00:00 - Nuit X » avant la nuit (prévu, pas encore utilisé). */
    NIGHT_INTRO,
    /** Nuit en cours. */
    PLAYING,
    /** Nuit mise en pause par le joueur. */
    PAUSED,
    /** Un clown attaque : animation avant la défaite. */
    JUMPSCARE,
    /** Nuit perdue. */
    GAME_OVER,
    /** 06:00 atteint : nuit gagnée. */
    WIN,
    /** Fin de l'histoire après la nuit 5 : journal du lendemain, puis générique. */
    ENDING
}
