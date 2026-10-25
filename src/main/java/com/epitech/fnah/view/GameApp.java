package com.epitech.fnah.view;

import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fenêtre du jeu. Crée la partie, les ressources, le son et les écrans,
 * transmet le clavier et la souris à l'écran actif, fait tourner la boucle
 * à pas fixe (60 ticks par seconde) et sauvegarde la progression.
 */
public class GameApp extends Application {

    /** Largeur de la fenêtre en pixels. */
    public static final double WIDTH = 1280;
    /** Hauteur de la fenêtre en pixels. */
    public static final double HEIGHT = 720;

    private static final double STEP = 1.0 / 60.0;
    private static final double MAX_FRAME = 0.25;

    private Game game;
    private GameConfig config;

    /** Constructeur appelé par JavaFX au lancement. */
    public GameApp() {
    }

    @Override
    public void start(Stage stage) {
        config = GameConfig.inUserHome();
        try {
            config.load();
        } catch (IOException e) {
            System.err.println("Progression illisible, départ à la nuit 1 : " + e.getMessage());
        }

        game = new Game();
        game.selectNight(config.getUnlockedNight());
        game.getEvents().subscribe(GameEvent.StateChanged.class, e -> {
            if (e.state() == GameState.WIN) {
                onNightWon();
            }
        });

        Assets assets = Assets.load();
        SoundManager sound = new SoundManager(game);
        ScreenManager screens = new ScreenManager(game, assets, sound, config);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        Scene scene = new Scene(new StackPane(canvas), WIDTH, HEIGHT, Color.BLACK);
        // Une touche maintenue ne compte qu'une fois : sans cela, la répétition
        // automatique du clavier ouvrirait et fermerait la porte en boucle.
        Set<KeyCode> held = EnumSet.noneOf(KeyCode.class);
        scene.setOnKeyReleased(e -> held.remove(e.getCode()));
        scene.setOnKeyPressed(e -> {
            if (!held.add(e.getCode())) {
                return;
            }
            if (e.getCode() == KeyCode.M) {
                sound.toggleMute();
            } else {
                screens.onKey(e);
            }
        });
        stage.focusedProperty().addListener((obs, was, now) -> held.clear());
        canvas.setOnMouseClicked(e -> screens.onClick(e.getX(), e.getY()));
        canvas.setOnMouseMoved(e -> screens.onMouseMove(e.getX(), e.getY()));

        stage.setTitle("Minuit au Cirque Malvaux");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        var gc = canvas.getGraphicsContext2D();
        new AnimationTimer() {
            private long last = -1;
            private double accumulator = 0;

            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    return;
                }
                accumulator += Math.min((now - last) / 1e9, MAX_FRAME);
                last = now;
                while (accumulator >= STEP) {
                    game.tick(STEP * screens.timeScale());
                    screens.update(STEP);
                    accumulator -= STEP;
                }
                screens.render(gc);
            }
        }.start();
    }

    private void onNightWon() {
        if (!game.hasNextNight()) {
            return;
        }
        config.unlockNight(game.getCurrentNight() + 1);
        try {
            config.save();
        } catch (IOException e) {
            System.err.println("Impossible de sauvegarder la progression : " + e.getMessage());
        }
    }
}
