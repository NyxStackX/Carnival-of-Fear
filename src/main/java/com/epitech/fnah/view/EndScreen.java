package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.entity.Henry;
import com.epitech.fnah.model.Room;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Fin de nuit : écran de défaite (« Nuit perdue ») ou de victoire (06:00),
 * avec les choix pour continuer.
 */
final class EndScreen implements Screen {

    private static final double HENRY_ENTRY_DURATION = 4.2;

    private final Game game;
    private final Assets assets;
    private final SoundManager sound;
    private final boolean victory;
    private final OptionList options;
    private javafx.scene.image.Image background;
    private double time;
    private String survivedUntil = "00:00";

    EndScreen(Game game, Assets assets, SoundManager sound, boolean victory) {
        this.game = game;
        this.assets = assets;
        this.sound = sound;
        this.victory = victory;
        this.options = new OptionList(Draw.W / 2 - 150, 520, 50, 340, sound::menuMove, sound::menuSelect);
    }

    @Override
    public void onEnter() {
        time = 0;
        options.clear();
        if (!victory) {
            options.add(() -> "REJOUER LA NUIT", game::restartNight);
            options.add(() -> "MENU", game::returnToMenu);
        } else if (game.hasNextNight()) {
            options.add(() -> "NUIT SUIVANTE", game::startNextNight);
            options.add(() -> "MENU", this::backToMenu);
        } else {
            options.add(() -> "RETOUR AU MENU", this::backToMenu);
        }
        survivedUntil = game.getClock().getFormattedTime();
    }

    @Override
    public void update(double dt) {
        time += dt;
        options.update(dt);
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isHenryIntroPlaying()) {
            Draw.clear(gc, Color.BLACK);
            Draw.grain(gc, 360, 0.12);
            return;
        }
        Draw.clear(gc, Color.BLACK);
        double fadeIn = Math.min(1.0, time / 1.2);
        gc.setGlobalAlpha(fadeIn);
        if (background == null) {
            background = Draw.render(Draw.W, Draw.H, g -> {
                Draw.cover(g, assets.camera(victory ? Room.PISTE_CENTRALE : Room.COULOIR_GAUCHE), 0, 0,
                        Draw.W, Draw.H, 1.05);
                Draw.shade(g, victory ? 0.55 : 0.75);
            });
        }
        gc.drawImage(background, 0, 0);

        double cx = Draw.W / 2;
        if (victory) {
            gc.setFill(Color.rgb(255, 200, 120, 0.06));
            gc.fillRect(0, 0, Draw.W, Draw.H);
            Draw.shadowText(gc, "06:00", cx, 210, assets.title(150), Draw.CREAM, TextAlignment.CENTER);
            Draw.shadowText(gc, "NUIT " + game.getCurrentNight() + " TERMINÉE", cx, 330, assets.title(44),
                    Draw.GOLD, TextAlignment.CENTER);
            String line = game.hasNextNight()
                    ? "Le jour se lève sur le chapiteau. Les clowns retournent à leur place... pour l'instant."
                    : "Cinq nuits. Vous êtes sorti vivant du Cirque Malvaux.";
            Draw.text(gc, line, cx, 400, assets.ui(19), Draw.TEXT, TextAlignment.CENTER);
        } else {
            gc.save();
            gc.setGlobalAlpha(0.35 * fadeIn);
            if (game.getPower().isPowerOut()) {
                String frame = new String[]{"idle_1", "idle_2", "idle_3"}[(int) (time * 1.4) % 3];
                Draw.spriteAt(gc, assets.sprite(Henry.SPRITE_ID, frame, 560), 1060, Draw.H + 20);
            } else {
                Draw.spriteAt(gc, assets.sprite("bobo", "idle_3", 560), 1060, Draw.H + 20);
            }
            gc.restore();
            Draw.shadowText(gc, "NUIT PERDUE", cx, 230, assets.title(96), Draw.RED, TextAlignment.CENTER);
            String attacker = game.getAttackerName();
            String reason = attacker != null
                    ? attacker + " vous a trouvé à " + survivedUntil + "."
                    : "Panne de courant à " + survivedUntil + ". Plus rien ne les arrêtait.";
            Draw.text(gc, reason, cx, 330, assets.ui(22), Draw.TEXT, TextAlignment.CENTER);
            Draw.text(gc, "Nuit " + game.getCurrentNight(), cx, 372, assets.ui(17), Draw.TEXT_DIM, TextAlignment.CENTER);
        }
        options.render(gc, assets.ui(24), assets.ui(28));
        gc.setGlobalAlpha(1.0);

        Draw.grain(gc, 350, 0.15);
        Draw.overlay(gc, 0, 0.15, 0.9);
    }

    @Override
    public void onKey(KeyEvent event) {
        if (time < (isHenryIntroPlaying() ? HENRY_ENTRY_DURATION : 0.6)) {
            return;
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            backToMenu();
        } else {
            options.onKey(event.getCode());
        }
    }

    @Override
    public void onClick(double x, double y) {
        if (time >= (isHenryIntroPlaying() ? HENRY_ENTRY_DURATION : 0.6)) {
            options.onClick(x, y);
        }
    }

    @Override
    public void onMouseMove(double x, double y) {
        options.onMouseMove(x, y);
    }

    private void backToMenu() {
        if (victory && game.hasNextNight() && game.getState() == GameState.WIN) {
            game.selectNight(game.getCurrentNight() + 1);
        }
        game.returnToMenu();
    }

    private boolean isHenryIntroPlaying() {
        return game.getPower().isPowerOut() && sound.isHenryEntryAvailable() && time < HENRY_ENTRY_DURATION;
    }
}
