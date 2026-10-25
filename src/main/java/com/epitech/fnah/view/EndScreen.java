package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.entity.Henry;
import com.epitech.fnah.entity.Pepin;
import com.epitech.fnah.model.Room;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Fin de nuit : écran de défaite (« Nuit perdue ») ou de victoire (06:00),
 * avec les choix pour continuer. Après la nuit 5, la victoire mène à la fin
 * de l'histoire.
 */
final class EndScreen implements Screen {

    private final Game game;
    private final Assets assets;
    private final boolean victory;
    private final OptionList options;
    private javafx.scene.image.Image background;
    private double time;
    private String survivedUntil = "00:00";

    EndScreen(Game game, Assets assets, SoundManager sound, boolean victory) {
        this.game = game;
        this.assets = assets;
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
        } else if (game.canPlayEnding()) {
            options.add(() -> "LE LENDEMAIN...", game::playEnding);
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
        String nightName = game.isCustomNight() ? "Nuit personnalisée" : "Nuit " + game.getCurrentNight();
        if (victory) {
            gc.setFill(Color.rgb(255, 200, 120, 0.06));
            gc.fillRect(0, 0, Draw.W, Draw.H);
            Draw.shadowText(gc, "06:00", cx, 210, assets.title(150), Draw.CREAM, TextAlignment.CENTER);
            Draw.shadowText(gc, nightName.toUpperCase() + " TERMINÉE", cx, 330, assets.title(44),
                    Draw.GOLD, TextAlignment.CENTER);
            String line = game.hasNextNight()
                    ? "Le jour se lève sur le chapiteau. Les clowns retournent à leur place... pour l'instant."
                    : game.isCustomNight()
                    ? "La troupe salue. Vous aussi, vous faites partie du spectacle."
                    : "Cinq nuits. Vous êtes sorti vivant du Cirque Malvaux. Du moins, c'est ce que vous croyez.";
            Draw.text(gc, line, cx, 400, assets.ui(19), Draw.TEXT, TextAlignment.CENTER);
        } else {
            String attacker = game.getAttackerName();
            gc.save();
            gc.setGlobalAlpha(0.35 * fadeIn);
            String sprite = attacker == null ? "bobo" : switch (attacker) {
                case Henry.NAME -> Henry.SPRITE_ID;
                case Pepin.NAME -> Pepin.SPRITE_ID;
                case "Mimi" -> "mimi";
                case "Zig et Zag" -> "zig_et_zag";
                default -> "bobo";
            };
            String frame = new String[]{"idle_1", "idle_2", "idle_3"}[(int) (time * 1.4) % 3];
            Draw.spriteAt(gc, assets.sprite(sprite, frame, 560), 1060, Draw.H + 20);
            gc.restore();
            Draw.shadowText(gc, "NUIT PERDUE", cx, 230, assets.title(96), Draw.RED, TextAlignment.CENTER);
            String reason;
            if (Henry.NAME.equals(attacker)) {
                reason = "Plus de courant. Henry a fini sa mélodie à " + survivedUntil + ".";
            } else if (Pepin.NAME.equals(attacker)) {
                reason = "L'orgue s'est tu. Pépin est sorti de sa boîte à " + survivedUntil + ".";
            } else if (attacker != null) {
                reason = attacker + " vous a trouvé à " + survivedUntil + ".";
            } else {
                reason = "La nuit s'est arrêtée à " + survivedUntil + ".";
            }
            Draw.text(gc, reason, cx, 330, assets.ui(22), Draw.TEXT, TextAlignment.CENTER);
            Draw.text(gc, nightName, cx, 372, assets.ui(17), Draw.TEXT_DIM, TextAlignment.CENTER);
        }
        options.render(gc, assets.ui(24), assets.ui(28));
        gc.setGlobalAlpha(1.0);

        Draw.grain(gc, 350, 0.15);
        Draw.overlay(gc, 0, 0.15, 0.9);
    }

    @Override
    public void onKey(KeyEvent event) {
        if (time < 0.6) {
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
        if (time >= 0.6) {
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
}
