package com.epitech.fnah;

import com.epitech.fnah.view.GameApp;
import javafx.application.Application;

/**
 * Lanceur du jeu. Séparé de {@link GameApp} pour pouvoir lancer le jeu
 * depuis un IDE ou un jar sans configuration de modules JavaFX.
 */
public final class Main {

    private Main() {
    }

    /**
     * Lance la fenêtre du jeu.
     *
     * @param args arguments transmis à JavaFX
     */
    public static void main(String[] args) {
        Application.launch(GameApp.class, args);
    }
}
