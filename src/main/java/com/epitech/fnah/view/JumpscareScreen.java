package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameLoop;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * Jumpscare : le clown qui a attaqué hurle en plein écran, l'image tremble,
 * clignote en rouge et se brouille, pendant {@link GameLoop#JUMPSCARE_DURATION} secondes.
 */
final class JumpscareScreen implements Screen {

    /** Dossier d'images de chaque clown, d'après son nom. */
    private static final Map<String, String> SPRITES = Map.of(
            "Bobo", "bobo", "Mimi", "mimi", "Zig et Zag", "zig_et_zag", "Pépin", "pepin", "Henry", "henry");

    private final Game game;
    private final Assets assets;
    private double time;

    JumpscareScreen(Game game, Assets assets) {
        this.game = game;
        this.assets = assets;
    }

    @Override
    public void onEnter() {
        time = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
    }

    @Override
    public void render(GraphicsContext gc) {
        boolean flash = ((int) (time * 14)) % 2 == 0;
        Draw.clear(gc, flash ? Color.web("#2a0006") : Color.BLACK);

        String attacker = game.getAttackerName();
        String character = attacker == null ? "bobo" : SPRITES.getOrDefault(attacker, "bobo");
        String frame = time < 0.22 ? "scare_1" : time < 0.7 || assets.sprite(character, "scare_3") == null
                ? "scare_2" : "scare_3";
        Image image = assets.sprite(character, frame);
        if (image == null) {
            image = assets.sprite("bobo", "scare_2");
        }
        double grow = Math.min(1.0, time / (GameLoop.JUMPSCARE_DURATION * 0.5));
        double height = Draw.H * (1.6 + 0.9 * grow);
        double shake = 22 * (1 - grow * 0.5);
        double cx = Draw.W / 2 + Draw.random(-shake, shake);
        double bottom = height * 0.88 + Draw.random(-shake, shake);

        Draw.sprite(gc, image, cx, bottom, height);

        gc.setFill(Color.rgb(200, 0, 20, flash ? 0.22 : 0.08));
        gc.fillRect(0, 0, Draw.W, Draw.H);
        // Fin de la capture : l'image se coupe dans la neige, puis le noir
        double end = GameLoop.JUMPSCARE_DURATION - time;
        if (end < 0.35) {
            Draw.clear(gc, Color.BLACK);
            Draw.grain(gc, 2600, 0.5 * Math.max(0, end / 0.35));
            Ui.vhsTracking(gc, time * 8);
        }
        Draw.grain(gc, 1200, 0.5);
        Draw.overlay(gc, 0, 0.3, 0.95);
    }
}
