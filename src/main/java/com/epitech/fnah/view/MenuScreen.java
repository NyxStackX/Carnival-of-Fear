package com.epitech.fnah.view;

import com.epitech.fnah.config.Achievement;
import com.epitech.fnah.config.CustomNight;
import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.config.NightConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.SecretCode;
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
 * de cirque, Pépin qui se balance et surgit de temps en temps, options animées,
 * étoiles obtenues, nuit personnalisée et page « Comment jouer ».
 *
 * <p>Easter egg : taper HONK fait surgir Bobo.</p>
 */
final class MenuScreen implements Screen {

    private static final String[] HELP = {
        "Survivez de minuit à 6 h dans la régie du Cirque Malvaux.",
        "",
        "[Espace]  Lever / baisser la tablette des caméras",
        "[1-8] ou [Gauche / Droite]  Changer de caméra",
        "[Q] / [E]  Fermer / ouvrir la porte gauche / droite",
        "[A] / [D]  Maintenir pour allumer la lampe du couloir",
        "[R]  Maintenir sur la CAM 8 pour remonter l'orgue",
        "",
        "Les lampes montrent qui attend derrière chaque porte.",
        "Si l'orgue s'arrête, Pépin sort : les portes n'y feront rien.",
        "Tout consomme de l'énergie. À 0 %, il fait noir... et Henry vient.",
        "",
        "[P] Pause      [M] Couper le son      [Échap] Menu",
    };
    private static final String[] CLOWNS = {"bobo", "mimi", "zig_et_zag", "pepin"};
    private static final String[] CLOWN_NAMES = {"BOBO", "MIMI", "ZIG ET ZAG", "PÉPIN"};
    private static final double DRIFT = 20;
    private static final double HONK_DURATION = 1.8;

    private final Game game;
    private final Assets assets;
    private final SoundManager sound;
    private final GameConfig config;
    private final OptionList options;
    private final OptionList customOptions;
    private final SecretCode honkCode = new SecretCode("HONK");

    private Image background;
    private Image title;
    private double time;
    private boolean helpOpen;
    private boolean starsOpen;
    private boolean customOpen;
    private CustomNight custom = CustomNight.standard();
    private double nextScare = 9;
    private double scareTimer;
    private double flicker;
    private double honkTimer;
    private double lockedTimer;

    MenuScreen(Game game, Assets assets, SoundManager sound, GameConfig config) {
        this.game = game;
        this.assets = assets;
        this.sound = sound;
        this.config = config;
        this.options = new OptionList(120, 400, 38, 470, sound::menuMove, sound::menuSelect)
                .add(() -> "JOUER", () -> game.startNight(game.getCurrentNight()))
                .add(this::nightLabel, () -> changeNight(1), this::changeNight)
                .add(() -> config.isCustomNightUnlocked() ? "NUIT PERSONNALISÉE" : "NUIT PERSONNALISÉE (verrouillée)",
                        this::openCustom)
                .add(() -> "ÉTOILES  " + config.getAchievements().size() + " / " + Achievement.values().length,
                        () -> starsOpen = true)
                .add(() -> "COMMENT JOUER", () -> helpOpen = true)
                .add(() -> "SON : " + (!sound.isAvailable() ? "INDISPONIBLE" : sound.isMuted() ? "COUPÉ" : "ACTIVÉ"),
                        sound::toggleMute)
                .add(() -> "QUITTER", Platform::exit);
        this.customOptions = new OptionList(330, 250, 46, 420, sound::menuMove, sound::menuSelect);
        for (int i = 0; i < CustomNight.CLOWN_COUNT; i++) {
            int index = i;
            customOptions.add(() -> String.format("%-11s <  %2d  >", CLOWN_NAMES[index], custom.level(index)),
                    () -> custom = custom.adjust(index, 1), step -> custom = custom.adjust(index, step));
        }
        customOptions.add(() -> "DÉFI : 20 / 20 / 20 / 20", () -> custom = CustomNight.maxChallenge())
                .add(() -> "LANCER LA NUIT", () -> game.startCustomNight(custom))
                .add(() -> "RETOUR", () -> customOpen = false);
    }

    @Override
    public void onEnter() {
        helpOpen = false;
        starsOpen = false;
        customOpen = false;
        options.reset();
        time = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
        options.update(dt);
        customOptions.update(dt);
        honkTimer = Math.max(0, honkTimer - dt);
        lockedTimer = Math.max(0, lockedTimer - dt);
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
            title = Draw.render(620, 400, this::paintTitle);
        }
        // Positions entières : un décalage à virgule forcerait un lissage coûteux à chaque image
        gc.drawImage(background, Math.round(-DRIFT + DRIFT * Math.sin(time * 0.07)),
                Math.round(-DRIFT + DRIFT * Math.cos(time * 0.05)));

        drawPepin(gc);
        drawHonk(gc);

        double jitter = scareTimer > 0 ? Draw.random(-4, 4) : 0;
        gc.drawImage(title, Math.round(96 + jitter), -20);

        options.render(gc, assets.ui(24), assets.ui(27));
        drawStars(gc, Draw.W - 40, 40);

        Draw.text(gc, "Nuits débloquées : " + config.getUnlockedNight() + " / " + NightConfig.LAST_NIGHT,
                112, Draw.H - 30, assets.ui(15), Draw.TEXT_DIM, TextAlignment.LEFT);
        Draw.text(gc, "[Haut / Bas] Choisir   [Entrée] Valider   [M] Son", Draw.W - 40, Draw.H - 30,
                assets.ui(15), Draw.TEXT_DIM, TextAlignment.RIGHT);
        if (!sound.isAvailable()) {
            Draw.text(gc, "Aucune sortie audio détectée : voir la section Son du README", 112, Draw.H - 56,
                    assets.ui(15), Draw.AMBER, TextAlignment.LEFT);
        }
        if (lockedTimer > 0) {
            Draw.text(gc, "Terminez la nuit 5 pour débloquer la nuit personnalisée.", 600, 476,
                    assets.ui(16), Draw.AMBER, TextAlignment.LEFT);
        }

        // Pellicule abîmée
        Draw.grain(gc, 400, 0.18);
        Draw.overlay(gc, 0, 0.18, 0.9);
        if (flicker > 0) {
            Draw.shade(gc, flicker);
        }

        if (helpOpen) {
            drawHelp(gc);
        } else if (starsOpen) {
            drawStarsPanel(gc);
        } else if (customOpen) {
            drawCustomPanel(gc);
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
        g.strokeLine(16, 362, 464, 362);
        Draw.text(g, "Cinq nuits. Une régie. Et la troupe n'est jamais repartie.", 16, 382,
                assets.ui(15), Draw.TEXT_DIM, TextAlignment.LEFT);
    }

    // ------------------------------------------------------------------ entrées

    @Override
    public void onKey(KeyEvent event) {
        KeyCode code = event.getCode();
        if (honkCode.type(event.getText())) {
            honkTimer = HONK_DURATION;
            sound.honk();
            sound.jackPop();
        }
        if (helpOpen || starsOpen) {
            if (code == KeyCode.ESCAPE || code == KeyCode.ENTER) {
                helpOpen = false;
                starsOpen = false;
                sound.menuMove();
            }
            return;
        }
        if (customOpen) {
            if (code == KeyCode.ESCAPE) {
                customOpen = false;
                sound.menuMove();
            } else {
                customOptions.onKey(code);
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
        if (helpOpen || starsOpen) {
            helpOpen = false;
            starsOpen = false;
            sound.menuMove();
        } else if (customOpen) {
            customOptions.onClick(x, y);
        } else {
            options.onClick(x, y);
        }
    }

    @Override
    public void onMouseMove(double x, double y) {
        if (customOpen) {
            customOptions.onMouseMove(x, y);
        } else if (!helpOpen && !starsOpen) {
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

    private void openCustom() {
        if (config.isCustomNightUnlocked()) {
            customOpen = true;
            customOptions.reset();
        } else {
            lockedTimer = 2.5;
        }
    }

    // ------------------------------------------------------------------ dessin

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

    /** Easter egg HONK : Bobo surgit du bas de l'écran. */
    private void drawHonk(GraphicsContext gc) {
        if (honkTimer <= 0) {
            return;
        }
        double t = HONK_DURATION - honkTimer;
        double rise = Math.min(1.0, t / 0.25) * Math.min(1.0, honkTimer / 0.3);
        Draw.spriteAt(gc, assets.sprite("bobo", "idle_2", 520), 740 + Draw.random(-3, 3),
                Math.round(Draw.H + 420 - 440 * rise));
        if (t > 0.15) {
            Draw.shadowText(gc, "HONK !", 760, 150, assets.title(84), Draw.GOLD, TextAlignment.CENTER);
        }
    }

    private void drawStars(GraphicsContext gc, double right, double top) {
        Achievement[] all = Achievement.values();
        for (int i = 0; i < all.length; i++) {
            double cx = right - 18 - (all.length - 1 - i) * 42;
            drawStar(gc, cx, top + 16, 16, config.isUnlocked(all[i]));
        }
    }

    private static void drawStar(GraphicsContext gc, double cx, double cy, double r, boolean lit) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int k = 0; k < 10; k++) {
            double radius = k % 2 == 0 ? r : r * 0.45;
            double angle = -Math.PI / 2 + k * Math.PI / 5;
            xs[k] = cx + Math.cos(angle) * radius;
            ys[k] = cy + Math.sin(angle) * radius;
        }
        gc.setFill(lit ? Draw.GOLD : Color.rgb(255, 255, 255, 0.08));
        gc.fillPolygon(xs, ys, 10);
        gc.setStroke(lit ? Draw.CREAM : Draw.TEXT_DIM);
        gc.setLineWidth(1.2);
        gc.strokePolygon(xs, ys, 10);
    }

    private void drawPanel(GraphicsContext gc, double x, double y, double w, double h, String heading) {
        Draw.shade(gc, 0.75);
        gc.setFill(Color.rgb(20, 14, 14, 0.95));
        gc.fillRect(x, y, w, h);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(x + 8, y + 8, w - 16, h - 16);
        Draw.shadowText(gc, heading, Draw.W / 2, y + 56, assets.title(40), Draw.RED, TextAlignment.CENTER);
    }

    private void drawHelp(GraphicsContext gc) {
        double x = 220;
        double y = 90;
        double w = Draw.W - 440;
        double h = Draw.H - 170;
        drawPanel(gc, x, y, w, h, "COMMENT JOUER");
        for (int i = 0; i < HELP.length; i++) {
            Draw.text(gc, HELP[i], x + 50, y + 112 + i * 29, assets.ui(18), Draw.TEXT, TextAlignment.LEFT);
        }
        Draw.text(gc, "[Entrée] Fermer", Draw.W / 2, y + h - 30, assets.ui(16), Draw.TEXT_DIM, TextAlignment.CENTER);
    }

    private void drawStarsPanel(GraphicsContext gc) {
        double x = 260;
        double y = 130;
        double w = Draw.W - 520;
        double h = Draw.H - 260;
        drawPanel(gc, x, y, w, h, "ÉTOILES");
        Achievement[] all = Achievement.values();
        for (int i = 0; i < all.length; i++) {
            boolean lit = config.isUnlocked(all[i]);
            double rowY = y + 130 + i * 76;
            drawStar(gc, x + 80, rowY, 22, lit);
            Draw.text(gc, all[i].getTitle(), x + 130, rowY - 12, assets.title(24), lit ? Draw.GOLD : Draw.TEXT_DIM,
                    TextAlignment.LEFT);
            Draw.text(gc, all[i].getDescription(), x + 130, rowY + 18, assets.ui(17), lit ? Draw.TEXT : Draw.TEXT_DIM,
                    TextAlignment.LEFT);
        }
        Draw.text(gc, "[Entrée] Fermer", Draw.W / 2, y + h - 30, assets.ui(16), Draw.TEXT_DIM, TextAlignment.CENTER);
    }

    private void drawCustomPanel(GraphicsContext gc) {
        double x = 260;
        double y = 110;
        double w = Draw.W - 520;
        double h = Draw.H - 200;
        drawPanel(gc, x, y, w, h, "NUIT PERSONNALISÉE");
        Draw.text(gc, "Réglez chaque clown de 0 (absent) à 20.", Draw.W / 2, y + 100, assets.ui(16),
                Draw.TEXT_DIM, TextAlignment.CENTER);
        customOptions.render(gc, assets.ui(22), assets.ui(25));
        int selected = customOptions.getSelected();
        if (selected < CustomNight.CLOWN_COUNT) {
            String frame = new String[]{"idle_1", "idle_2", "idle_3"}[(int) (time * 1.4) % 3];
            Draw.spriteAt(gc, assets.sprite(CLOWNS[selected], frame, 300), x + w - 130, y + h - 60);
        } else if (custom.isMaxChallenge()) {
            Draw.text(gc, "Bonne chance.", x + w - 130, y + h - 120, assets.title(26), Draw.RED, TextAlignment.CENTER);
        }
        Draw.text(gc, "[Gauche / Droite] Régler   [Entrée] Valider   [Échap] Retour", Draw.W / 2, y + h - 30,
                assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
    }
}
