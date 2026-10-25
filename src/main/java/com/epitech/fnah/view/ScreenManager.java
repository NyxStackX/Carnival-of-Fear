package com.epitech.fnah.view;

import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.config.Achievement;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayDeque;
import java.util.Deque;

import java.util.EnumMap;
import java.util.Map;

/**
 * Choisit l'écran à afficher selon l'état du jeu (patron State) et lui
 * transmet le temps, le dessin et les entrées. Affiche aussi, par-dessus
 * n'importe quel écran, l'annonce d'une étoile obtenue.
 */
public final class ScreenManager {

    private final Map<GameState, Screen> screens = new EnumMap<>(GameState.class);
    private static final double TOAST_DURATION = 4.0;

    private final Game game;
    private final Assets assets;
    private final NightScreen night;
    private final Deque<Achievement> toasts = new ArrayDeque<>();
    private double toastTimer;
    private Screen current;
    private GameState currentState;

    /**
     * Crée tous les écrans et suit les changements d'état de la partie.
     *
     * @param game   partie en cours
     * @param assets ressources graphiques
     * @param sound  sons du jeu
     * @param config progression du joueur
     */
    public ScreenManager(Game game, Assets assets, SoundManager sound, GameConfig config) {
        this.game = game;
        this.assets = assets;
        MenuScreen menu = new MenuScreen(game, assets, sound, config);
        night = new NightScreen(game, assets, sound);
        screens.put(GameState.MENU, menu);
        screens.put(GameState.NIGHT_INTRO, menu);
        screens.put(GameState.PLAYING, night);
        screens.put(GameState.PAUSED, night);
        screens.put(GameState.JUMPSCARE, new JumpscareScreen(game, assets));
        screens.put(GameState.GAME_OVER, new EndScreen(game, assets, sound, false));
        screens.put(GameState.WIN, new EndScreen(game, assets, sound, true));
        screens.put(GameState.ENDING, new EndingScreen(game, assets));
        game.getEvents().subscribe(GameEvent.StateChanged.class, e -> switchTo(e.state()));
        game.getEvents().subscribe(GameEvent.AchievementUnlocked.class, e -> toasts.add(e.achievement()));
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
        syncToGameState();
        if (game.getState() != GameState.PAUSED) {
            current.update(dt);
        }
        if (!toasts.isEmpty()) {
            toastTimer += dt;
            if (toastTimer >= TOAST_DURATION) {
                toasts.poll();
                toastTimer = 0;
            }
        }
    }

    /**
     * Dessine l'écran actif.
     *
     * @param gc contexte graphique du canvas
     */
    public void render(GraphicsContext gc) {
        syncToGameState();
        current.render(gc);
        drawToast(gc);
    }

    /** Bandeau « Étoile obtenue » en haut de l'écran. */
    private void drawToast(GraphicsContext gc) {
        Achievement achievement = toasts.peek();
        if (achievement == null) {
            return;
        }
        double slide = Math.min(1.0, Math.min(toastTimer, TOAST_DURATION - toastTimer) / 0.3);
        double y = -80 + 110 * slide;
        gc.setFill(Color.rgb(20, 14, 14, 0.94));
        gc.fillRoundRect(Draw.W / 2 - 260, y, 520, 70, 12, 12);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(Draw.W / 2 - 260, y, 520, 70, 12, 12);
        Draw.text(gc, "ÉTOILE OBTENUE : " + achievement.getTitle(), Draw.W / 2, y + 24, assets.title(22),
                Draw.GOLD, TextAlignment.CENTER);
        Draw.text(gc, achievement.getDescription(), Draw.W / 2, y + 50, assets.ui(16), Draw.TEXT,
                TextAlignment.CENTER);
    }

    /**
     * Transmet le relâchement d'une touche à l'écran actif.
     *
     * @param event touche relâchée
     */
    public void onKeyReleased(KeyEvent event) {
        syncToGameState();
        current.onKeyReleased(event);
    }

    /**
     * Transmet l'appui sur le bouton de la souris à l'écran actif.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    public void onMousePressed(double x, double y) {
        syncToGameState();
        current.onMousePressed(x, y);
    }

    /**
     * Transmet le relâchement du bouton de la souris à l'écran actif.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    public void onMouseReleased(double x, double y) {
        syncToGameState();
        current.onMouseReleased(x, y);
    }

    /**
     * Transmet une touche à l'écran actif.
     *
     * @param event touche pressée
     */
    public void onKey(KeyEvent event) {
        syncToGameState();
        current.onKey(event);
    }

    /**
     * Transmet un clic à l'écran actif.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    public void onClick(double x, double y) {
        syncToGameState();
        current.onClick(x, y);
    }

    /**
     * Transmet un mouvement de souris à l'écran actif.
     *
     * @param x abscisse
     * @param y ordonnée
     */
    public void onMouseMove(double x, double y) {
        syncToGameState();
        current.onMouseMove(x, y);
    }

    private void syncToGameState() {
        GameState state = game.getState();
        if (state != currentState) {
            switchTo(state);
        }
    }

    private void switchTo(GameState state) {
        Screen next = screens.get(state);
        GameState previousState = currentState;
        if (next != current) {
            current = next;
            current.onEnter();
        } else if (state == GameState.PLAYING && previousState != GameState.PAUSED) {
            current.onEnter();
        }
        currentState = state;
    }
}
