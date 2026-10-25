package com.epitech.fnah.view;

import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.config.NightConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.model.Room;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;

/**
 * Menu principal : photo du chapiteau qui avance lentement, titre en lettres
 * de cirque, Pépin qui se balance et surgit de temps en temps, options animées
 * et page « Comment jouer ».
 */
final class MenuScreen implements Screen {

    private static final String[] HELP = {
        "Survivez de minuit à 6 h dans la régie du Cirque Malvaux.",
        "",
        "[Espace]  Lever / baisser la tablette des caméras",
        "[1-8] ou [Gauche / Droite]  Changer de caméra",
        "[Q]  Fermer / ouvrir la porte gauche (tablette baissée)",
        "",
        "Bobo avance vers la porte gauche. Suivez-le sur les caméras",
        "et fermez la porte quand il est juste derrière.",
        "Chaque équipement consomme de l'énergie : à 0 %, tout s'éteint.",
        "",
        "[M]  Couper le son        [Échap]  Retour au menu",
    };

    private final Game game;
    private final Assets assets;
    private final SoundManager sound;
    private final GameConfig config;
    private final OptionList options;

    private static final double DRIFT = 20;

    private Image background;
    private Image title;
    private double time;
    private boolean helpOpen;
    private double nextScare = 9;
    private double scareTimer;
    private double flicker;

    MenuScreen(Game game, Assets assets, SoundManager sound, GameConfig config) {
        this.game = game;
        this.assets = assets;
        this.sound = sound;
        this.config = config;
        this.options = new OptionList(120, 430, 52, 470, sound::menuMove, sound::menuSelect)
                .add(() -> "JOUER", () -> game.startNight(game.getCurrentNight()))
                .add(this::nightLabel, () -> changeNight(1), this::changeNight)
                .add(() -> "COMMENT JOUER", () -> helpOpen = true)
                .add(() -> "SON : " + (!sound.isAvailable() ? "INDISPONIBLE" : sound.isMuted() ? "COUPÉ" : "ACTIVÉ"),
                        sound::toggleMute)
                .add(() -> "QUITTER", Platform::exit);
    }

    @Override
    public void onEnter() {
        helpOpen = false;
        options.reset();
        time = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
        options.update(dt);
        if (scareTimer > 0) {
            scareTimer -= dt;
        }
        nextScare -= dt;
        if (nextScare <= 0) {
            scareTimer = 0.45;
            nextScare = Draw.random(9, 16);
            sound.jackPop();
        }
        flicker = Math.random() < 0.015 ? Draw.random(0.2, 0.5) : Math.max(0, flicker - dt * 3);
    }

    @Override
    public void render(GraphicsContext gc) {
        // Fond précalculé, légèrement plus grand que l'écran : il dérive lentement
        if (background == null) {
            background = Draw.render(Draw.W + 2 * DRIFT, Draw.H + 2 * DRIFT, this::paintBackground);
            title = Draw.render(620, 420, this::paintTitle);
        }
        // Positions entières : un décalage à virgule forcerait un lissage coûteux à chaque image
        gc.drawImage(background, Math.round(-DRIFT + DRIFT * Math.sin(time * 0.07)),
                Math.round(-DRIFT + DRIFT * Math.cos(time * 0.05)));

        drawPepin(gc);

        double jitter = scareTimer > 0 ? Draw.random(-4, 4) : 0;
        gc.drawImage(title, Math.round(96 + jitter), 0);

        options.render(gc, assets.ui(26), assets.ui(30));

        Draw.text(gc, "Nuits débloquées : " + config.getUnlockedNight() + " / " + NightConfig.LAST_NIGHT,
                112, Draw.H - 40, assets.ui(15), Draw.TEXT_DIM, TextAlignment.LEFT);
        Draw.text(gc, "[Haut / Bas] Choisir   [Entrée] Valider   [M] Son", Draw.W - 40, Draw.H - 40,
                assets.ui(15), Draw.TEXT_DIM, TextAlignment.RIGHT);
        if (!sound.isAvailable()) {
            Draw.text(gc, "Aucune sortie audio détectée : voir la section Son du README", 112, Draw.H - 68,
                    assets.ui(15), Draw.AMBER, TextAlignment.LEFT);
        }

        // Pellicule abîmée
        Draw.grain(gc, 400, 0.18);
        Draw.overlay(gc, 0, 0.18, 0.9);
        if (flicker > 0) {
            Draw.shade(gc, flicker);
        }

        if (helpOpen) {
            drawHelp(gc);
        }
    }

    private void paintBackground(GraphicsContext g) {
        double w = Draw.W + 2 * DRIFT;
        double h = Draw.H + 2 * DRIFT;
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, w, h);
        Draw.cover(g, assets.camera(Room.PISTE_CENTRALE), 0, 0, w, h, 1.08);
        g.setFill(Color.rgb(0, 0, 0, 0.55));
        g.fillRect(0, 0, w, h);
        g.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(10, 4, 5, 0.95)), new Stop(0.55, Color.rgb(40, 6, 10, 0.55)),
                new Stop(1, Color.rgb(0, 0, 0, 0.2))));
        g.fillRect(0, 0, w, h);
    }

    private void paintTitle(GraphicsContext g) {
        Draw.shadowText(g, "MINUIT AU", 16, 128, assets.title(38), Draw.CREAM, TextAlignment.LEFT);
        Draw.shadowText(g, "CIRQUE", 8, 210, assets.title(104), Draw.RED, TextAlignment.LEFT);
        Draw.shadowText(g, "MALVAUX", 8, 315, assets.title(104), Draw.RED, TextAlignment.LEFT);
        g.setStroke(Draw.GOLD);
        g.setLineWidth(2);
        g.strokeLine(16, 372, 464, 372);
        Draw.text(g, "Cinq nuits. Une régie. Et la troupe n'est jamais repartie.", 16, 392,
                assets.ui(15), Draw.TEXT_DIM, TextAlignment.LEFT);
    }

    @Override
    public void onKey(KeyEvent event) {
        KeyCode code = event.getCode();
        if (helpOpen) {
            if (code == KeyCode.ESCAPE || code == KeyCode.ENTER) {
                helpOpen = false;
                sound.menuMove();
            }
            return;
        }
        if (code == KeyCode.ESCAPE) {
            Platform.exit();
        } else {
            options.onKey(code);
        }
    }

    @Override
    public void onClick(double x, double y) {
        if (helpOpen) {
            helpOpen = false;
            sound.menuMove();
        } else {
            options.onClick(x, y);
        }
    }

    @Override
    public void onMouseMove(double x, double y) {
        if (!helpOpen) {
            options.onMouseMove(x, y);
        }
    }

    private String nightLabel() {
        int night = game.getCurrentNight();
        String left = night > 1 ? "<  " : "   ";
        String right = night < config.getUnlockedNight() ? "  >" : "   ";
        return "NUIT " + left + night + right;
    }

    private void changeNight(int step) {
        int target = game.getCurrentNight() + step;
        if (target > config.getUnlockedNight()) {
            target = 1;
        } else if (target < 1) {
            target = config.getUnlockedNight();
        }
        game.selectNight(target);
    }

    private void drawPepin(GraphicsContext gc) {
        boolean scaring = scareTimer > 0;
        String frame;
        if (scaring) {
            frame = scareTimer > 0.25 ? "scare_1" : "scare_2";
        } else {
            frame = new String[]{"idle_1", "idle_2", "idle_3", "idle_2"}[(int) (time * 1.6) % 4];
        }
        Image image = assets.sprite("pepin", frame, scaring ? 760 : 640);
        double sway = Math.sin(time * 1.3) * 10;
        double cx = 930 + sway + (scaring ? Draw.random(-8, 8) : 0);
        Draw.spriteAt(gc, image, cx, Draw.H + 30);
        if (scaring) {
            gc.setFill(Color.rgb(179, 32, 47, 0.12));
            gc.fillRect(0, 0, Draw.W, Draw.H);
            Draw.grain(gc, 1200, 0.35);
        }
    }

    private void drawHelp(GraphicsContext gc) {
        Draw.shade(gc, 0.75);
        double x = 240;
        double y = 130;
        double w = Draw.W - 480;
        double h = Draw.H - 240;
        gc.setFill(Color.rgb(20, 14, 14, 0.95));
        gc.fillRect(x, y, w, h);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(x + 8, y + 8, w - 16, h - 16);
        Draw.shadowText(gc, "COMMENT JOUER", Draw.W / 2, y + 56, assets.title(40), Draw.RED, TextAlignment.CENTER);
        for (int i = 0; i < HELP.length; i++) {
            Draw.text(gc, HELP[i], x + 60, y + 120 + i * 30, assets.ui(18), Draw.TEXT, TextAlignment.LEFT);
        }
        Draw.text(gc, "[Entrée] Fermer", Draw.W / 2, y + h - 34, assets.ui(16), Draw.TEXT_DIM, TextAlignment.CENTER);
    }
}
