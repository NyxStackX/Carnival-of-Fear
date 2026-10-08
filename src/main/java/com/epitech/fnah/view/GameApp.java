package com.epitech.fnah.view;

import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Fenêtre du jeu : relie le clavier et la souris à {@link Game}, fait tourner
 * la boucle à pas fixe (60 ticks par seconde), demande l'affichage à
 * {@link GameRenderer} et sauvegarde la progression.
 */
public class GameApp extends Application {

    /** Largeur de la fenêtre en pixels. */
    public static final double WIDTH = 1280;
    /** Hauteur de la fenêtre en pixels. */
    public static final double HEIGHT = 720;

    private static final double STEP = 1.0 / 60.0;
    private static final double MAX_FRAME = 0.25;
    private static final double FAST_TIME_SCALE = 10.0;

    private Game game;
    private GameConfig config;
    private GameRenderer renderer;
    private double timeScale = 1.0;

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

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        renderer = new GameRenderer(canvas.getGraphicsContext2D(), game, config);

        Scene scene = new Scene(new StackPane(canvas), WIDTH, HEIGHT, Color.BLACK);
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));
        canvas.setOnMouseClicked(e -> {
            if (renderer.isOnLeftDoorButton(e.getX(), e.getY())) {
                game.toggleLeftDoor();
            }
        });

        stage.setTitle("Minuit au Cirque Malvaux");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

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
                    game.tick(STEP * timeScale);
                    renderer.update(STEP);
                    accumulator -= STEP;
                }
                renderer.render(timeScale);
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

    private void handleKey(KeyCode code) {
        switch (game.getState()) {
            case MENU -> {
                int night = game.getCurrentNight();
                if (code == KeyCode.ENTER) game.startNight(night);
                else if (code == KeyCode.LEFT && night > 1) game.selectNight(night - 1);
                else if (code == KeyCode.RIGHT && night < config.getUnlockedNight()) game.selectNight(night + 1);
                else if (code == KeyCode.ESCAPE) Platform.exit();
            }
            case PLAYING -> {
                if (code == KeyCode.Q) game.toggleLeftDoor();
                else if (code == KeyCode.F2) timeScale = timeScale > 1 ? 1.0 : FAST_TIME_SCALE;
                else if (code == KeyCode.ESCAPE) game.returnToMenu();
            }
            case GAME_OVER -> {
                if (code == KeyCode.ENTER) game.restartNight();
                else if (code == KeyCode.ESCAPE) game.returnToMenu();
            }
            case WIN -> {
                if (code == KeyCode.ENTER && game.hasNextNight()) {
                    game.startNextNight();
                } else if (code == KeyCode.ENTER || code == KeyCode.ESCAPE) {
                    if (game.hasNextNight()) game.selectNight(game.getCurrentNight() + 1);
                    game.returnToMenu();
                }
            }
            default -> {
                // Pas d'entrée pendant le jumpscare ou l'intro.
            }
        }
    }
}
