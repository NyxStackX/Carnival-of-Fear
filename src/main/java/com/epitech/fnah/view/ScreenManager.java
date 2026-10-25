package com.epitech.fnah.view;

import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;

import java.util.EnumMap;
import java.util.Map;

/**
 * Choisit l'écran à afficher selon l'état du jeu (patron State) et lui
 * transmet le temps, le dessin et les entrées.
 */
public final class ScreenManager {

    private final Map<GameState, Screen> screens = new EnumMap<>(GameState.class);
    private final NightScreen night;
    private Screen current;

    /**
     * Crée tous les écrans et suit les changements d'état de la partie.
     *
     * @param game   partie en cours
     * @param assets ressources graphiques
     * @param sound  sons du jeu
     * @param config progression du joueur
     */
    public ScreenManager(Game game, Assets assets, SoundManager sound, GameConfig config) {
        MenuScreen menu = new MenuScreen(game, assets, sound, config);
        night = new NightScreen(game, assets);
        screens.put(GameState.MENU, menu);
        screens.put(GameState.NIGHT_INTRO, menu);
        screens.put(GameState.PLAYING, night);
        screens.put(GameState.JUMPSCARE, new JumpscareScreen(game, assets));
        screens.put(GameState.GAME_OVER, new EndScreen(game, assets, sound, false));
        screens.put(GameState.WIN, new EndScreen(game, assets, sound, true));
        game.getEvents().subscribe(GameEvent.StateChanged.class, e -> switchTo(e.state()));
        switchTo(game.getState());
    }

    /**
     * Vitesse du temps de jeu (accélérée en mode test pendant la nuit).
     *
     * @return multiplicateur du temps
     */
    public double timeScale() {
        return current == night ? night.timeScale() : 1.0;
    }

    /**
     * Fait avancer les animations de l'écran actif.
     *
     * @param dt temps écoulé en secondes
     */
    public void update(double dt) {
        current.update(dt);
    }

    /**
     * Dessine l'écran actif.
     *
     * @param gc contexte graphique du canvas
     */
    public void render(GraphicsContext gc) {
        current.render(gc);
    }

    /**
     * Transmet une touche à l'écran actif.
     *
     * @param event touche pressée
     */
    public void onKey(KeyEvent event) {
        current.onKey(event);
    }

    /**
     * Transmet un clic à l'écran actif.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    public void onClick(double x, double y) {
        current.onClick(x, y);
    }

    /**
     * Transmet un mouvement de souris à l'écran actif.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    public void onMouseMove(double x, double y) {
        current.onMouseMove(x, y);
    }

    private void switchTo(GameState state) {
        Screen next = screens.get(state);
        if (next != current) {
            current = next;
            current.onEnter();
        } else if (state == GameState.PLAYING) {
            current.onEnter();
        }
    }
}
