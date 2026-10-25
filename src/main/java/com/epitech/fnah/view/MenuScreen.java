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
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;

/**
 * Menu principal : photo du chapiteau qui dérive lentement sous un projecteur,
 * enseigne lumineuse du cirque, Pépin qui se balance et surgit de temps en
 * temps, options, étoiles obtenues, nuit personnalisée et page « Comment jouer ».
 *
 * <p>Easter egg : taper HONK fait surgir Bobo.</p>
 */
final class MenuScreen implements Screen {

    private static final String[][] HELP = {
        {"Espace", "Lever / baisser la tablette des caméras"},
        {"1 - 8", "Choisir une caméra (ou Gauche / Droite, ou le plan)"},
        {"Q / E", "Fermer / ouvrir la porte gauche / droite"},
        {"A / D", "Maintenir pour éclairer le couloir gauche / droit"},
        {"R", "Maintenir sur la CAM 8 pour remonter l'orgue"},
        {"S", "Se cacher sous le bureau / en sortir"},
        {"F", "Maintenir sur le disjoncteur pour réarmer un fusible"},
        {"J", "Carnet du veilleur (clic : coffre, carnet, journal)"},
        {"P", "Pause"},
    };
    private static final String[] RULES = {
        "Survivez de minuit à 6 h. Un clown devant une porte ouverte entre : fermez-la à temps.",
        "Le bruit les attire. Si l'orgue s'arrête, remontez-le ou cachez-vous sans un bruit.",
        "Trouvez les chiffres griffonnés sur les caméras pour ouvrir le coffre (batterie de secours).",
    };
    private static final String[] CLOWNS = {"bobo", "mimi", "zig_et_zag", "pepin"};
    private static final String[] CLOWN_NAMES = {"BOBO", "MIMI", "ZIG ET ZAG", "PÉPIN"};
    private static final String[] CLOWN_ROLES = {
        "Porte gauche", "Porte droite, caméra 7", "Brouille les caméras", "Orgue de la caméra 8",
    };
    private static final double DRIFT = 20;
    private static final double HONK_DURATION = 1.8;
    // Enseigne du titre
    private static final double SIGN_X = 64, SIGN_Y = 30, SIGN_W = 540, SIGN_H = 304;
    // Panneau des options
    private static final double LIST_X = 64, LIST_Y = 352, LIST_W = 540, LIST_H = 300;
    private static final double LIST_YFIRST = LIST_Y + 34;
    private static final double FOG_H = 340;

    private final Game game;
    private final Assets assets;
    private final SoundManager sound;
    private final GameConfig config;
    private final OptionList options;
    private final OptionList customOptions;
    private final SecretCode honkCode = new SecretCode("HONK");

    private Image background;
    private Image sign;
    private Image spotlight;
    private Image footer;
    private Image listPanel;
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
    private double subliminal;
    private double nextSubliminal = 14;
    private Image fog;

    MenuScreen(Game game, Assets assets, SoundManager sound, GameConfig config, Runnable playIntro) {
        this.game = game;
        this.assets = assets;
        this.sound = sound;
        this.config = config;
        this.options = new OptionList(LIST_X + 44, LIST_YFIRST, 33, LIST_W - 40, sound::menuMove, sound::menuSelect)
                .add(() -> "JOUER", () -> game.startNight(game.getCurrentNight()))
                .add(() -> "NUIT|" + game.getCurrentNight(), () -> changeNight(1), this::changeNight)
                .add(() -> config.isCustomNightUnlocked() ? "NUIT PERSONNALISÉE" : "NUIT PERSONNALISÉE  (verrouillée)",
                        this::openCustom)
                .add(() -> "ÉTOILES|" + config.getAchievements().size() + " / " + Achievement.values().length,
                        () -> starsOpen = true)
                .add(() -> "PROLOGUE", playIntro)
                .add(() -> "COMMENT JOUER", () -> helpOpen = true)
                .add(() -> "SON|" + (!sound.isAvailable() ? "ABSENT" : sound.isMuted() ? "COUPÉ" : sound.getVolume() + " %"),
                        sound::toggleMute, this::changeVolume)
                .add(() -> "QUITTER", Platform::exit);
        this.customOptions = new OptionList(330, 262, 44, 440, sound::menuMove, sound::menuSelect);
        for (int i = 0; i < CustomNight.CLOWN_COUNT; i++) {
            int index = i;
            customOptions.add(() -> CLOWN_NAMES[index] + "|" + custom.level(index),
                    () -> custom = custom.adjust(index, 1), step -> custom = custom.adjust(index, step));
        }
        customOptions.add(() -> "DÉFI 20 / 20 / 20 / 20", () -> custom = CustomNight.maxChallenge())
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
        flicker = Math.random() < 0.012 ? Draw.random(0.15, 0.4) : Math.max(0, flicker - dt * 3);
        // Image subliminale : un visage de clown, deux images à peine
        subliminal = Math.max(0, subliminal - dt);
        nextSubliminal -= dt;
        if (nextSubliminal <= 0) {
            nextSubliminal = Draw.random(18, 32);
            subliminal = 0.07;
        }
    }

    /** Nappes de brouillard, raccordées pour défiler sans couture. */
    private void paintFog(GraphicsContext g) {
        java.util.Random random = new java.util.Random(31);
        for (int i = 0; i < 70; i++) {
            double r = 60 + random.nextDouble() * 160;
            double x = random.nextDouble() * Draw.W;
            double y = 120 + random.nextDouble() * (FOG_H - 60);
            for (double ox : new double[]{x, x - Draw.W, x + Draw.W}) {
                g.setFill(new RadialGradient(0, 0, ox, y, r, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(150, 140, 135, 0.06)), new Stop(1, Color.TRANSPARENT)));
                g.fillOval(ox - r, y - r, r * 2, r * 2);
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // Fond et enseigne précalculés : seuls les éléments animés sont redessinés
        if (background == null) {
            background = Draw.render(Draw.W + 2 * DRIFT, Draw.H + 2 * DRIFT, this::paintBackground);
            sign = Draw.render(SIGN_W + 40, SIGN_H + 80, this::paintSign);
            fog = Draw.render(Draw.W, FOG_H, this::paintFog);
        }
        // Positions entières : un décalage à virgule forcerait un lissage coûteux à chaque image
        gc.drawImage(background, Math.round(-DRIFT + DRIFT * Math.sin(time * 0.07)),
                Math.round(-DRIFT + DRIFT * Math.cos(time * 0.05)));
        double fx = (time * 14) % Draw.W;
        gc.setGlobalAlpha(0.55);
        gc.drawImage(fog, Math.round(-fx), Draw.H - FOG_H);
        gc.drawImage(fog, Math.round(Draw.W - fx), Draw.H - FOG_H);
        gc.setGlobalAlpha(1.0);
        drawSpotlight(gc);
        drawPepin(gc);
        drawHonk(gc);
        if (subliminal > 0) {
            gc.setGlobalAlpha(0.3);
            Draw.spriteAt(gc, assets.sprite("bobo", "scare_2", 760), Draw.W / 2 + 160, Draw.H + 60);
            gc.setGlobalAlpha(1.0);
            gc.setFill(Color.rgb(140, 0, 10, 0.22));
            gc.fillRect(0, 0, Draw.W, Draw.H);
        }

        double jitter = scareTimer > 0 ? Draw.random(-4, 4) : 0;
        gc.drawImage(sign, Math.round(SIGN_X - 20 + jitter), SIGN_Y - 20);
        Ui.bulbs(gc, SIGN_X + 10, SIGN_Y + 10, SIGN_W - 20, SIGN_H - 20, 30, scareTimer > 0 ? -1 : time);

        if (listPanel == null) {
            listPanel = Draw.render(LIST_W, LIST_H, g -> Ui.panel(g, 0, 0, LIST_W, LIST_H));
        }
        gc.drawImage(listPanel, LIST_X, LIST_Y);
        options.render(gc, assets.ui(22), assets.ui(25));
        if (lockedTimer > 0) {
            Ui.alert(gc, "Terminez la nuit 5 pour débloquer la nuit personnalisée", LIST_X + LIST_W / 2,
                    LIST_Y + LIST_H + 26, assets.ui(15), time);
        }

        drawStarsBadge(gc);
        drawFooter(gc);

        // Pellicule abîmée
        Draw.grain(gc, 360, 0.18);
        Ui.filmDamage(gc);
        Draw.overlay(gc, 0, 0.16, 0.92);
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
        g.setGlobalBlendMode(javafx.scene.effect.BlendMode.MULTIPLY);
        g.setFill(Color.web("#8a3a30"));
        g.fillRect(0, 0, w, h);
        g.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
        g.setFill(Color.rgb(10, 0, 2, 0.55));
        g.fillRect(0, 0, w, h);
        g.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(8, 3, 4, 0.96)), new Stop(0.5, Color.rgb(35, 5, 9, 0.6)),
                new Stop(1, Color.rgb(0, 0, 0, 0.25))));
        g.fillRect(0, 0, w, h);
        g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.75, Color.TRANSPARENT), new Stop(1, Color.rgb(0, 0, 0, 0.85))));
        g.fillRect(0, 0, w, h);
    }

    /** Enseigne du cirque : panneau rouge, double filet doré, titre et devise. */
    private void paintSign(GraphicsContext g) {
        double x = 20;
        double y = 20;
        g.setFill(Color.rgb(0, 0, 0, 0.5));
        g.fillRoundRect(x + 8, y + 12, SIGN_W, SIGN_H, 22, 22);
        g.setFill(new LinearGradient(0, y, 0, y + SIGN_H, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a0c14")), new Stop(1, Color.web("#1e0508"))));
        g.fillRoundRect(x, y, SIGN_W, SIGN_H, 22, 22);
        g.setStroke(Draw.GOLD);
        g.setLineWidth(3);
        g.strokeRoundRect(x + 1.5, y + 1.5, SIGN_W - 3, SIGN_H - 3, 22, 22);
        g.setStroke(Color.rgb(201, 162, 74, 0.45));
        g.setLineWidth(1);
        g.strokeRoundRect(x + 26, y + 26, SIGN_W - 52, SIGN_H - 52, 12, 12);
        // Rayons de chapiteau, très discrets, derrière le titre
        g.setStroke(Color.rgb(255, 255, 255, 0.03));
        g.setLineWidth(18);
        for (int i = -6; i <= 6; i++) {
            g.strokeLine(x + SIGN_W / 2, y + SIGN_H + 140, x + SIGN_W / 2 + i * 70, y - 20);
        }
        // Enseigne abîmée : taches de suie et de moisissure
        java.util.Random random = new java.util.Random(1994);
        for (int i = 0; i < 26; i++) {
            double r = 8 + random.nextDouble() * 50;
            double sx = x + random.nextDouble() * SIGN_W;
            double sy = y + random.nextDouble() * SIGN_H;
            g.setFill(new RadialGradient(0, 0, sx, sy, r, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(0, 0, 0, 0.45)), new Stop(1, Color.TRANSPARENT)));
            g.fillOval(sx - r, sy - r, r * 2, r * 2);
        }
        double cx = x + SIGN_W / 2;
        Draw.shadowText(g, "M I N U I T   A U", cx, y + 64, assets.serif(28), Draw.CREAM, TextAlignment.CENTER);
        Ui.diamond(g, cx - 150, y + 66, 4, Draw.GOLD);
        Ui.diamond(g, cx + 150, y + 66, 4, Draw.GOLD);
        Ui.ghostText(g, "CIRQUE", cx, y + 128, assets.title(78), Color.web("#d8202f"), TextAlignment.CENTER, 2);
        Ui.ghostText(g, "MALVAUX", cx, y + 198, assets.title(78), Color.web("#d8202f"), TextAlignment.CENTER, 2);
        g.setStroke(Color.rgb(201, 162, 74, 0.6));
        g.setLineWidth(1);
        g.strokeLine(cx - 150, y + 240, cx + 150, y + 240);
        Ui.diamond(g, cx, y + 240, 4, Draw.GOLD);
        Draw.text(g, "Cinq nuits. Une régie. Et la troupe n'est jamais repartie.", cx, y + 262,
                assets.ui(15), Draw.TEXT, TextAlignment.CENTER);
        // Le sang coule du bas de l'enseigne
        Ui.drips(g, x + 40, y + SIGN_H - 2, SIGN_W - 80, 46, 31);
    }

    /** Faisceau de projecteur qui balaie lentement la piste. */
    private void drawSpotlight(GraphicsContext gc) {
        if (spotlight == null) {
            spotlight = Draw.render(520, 520, g -> {
                g.setFill(new RadialGradient(0, 0, 260, 260, 260, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 190, 140, 0.10)), new Stop(1, Color.TRANSPARENT)));
                g.fillOval(0, 0, 520, 520);
            });
        }
        gc.drawImage(spotlight, Math.round(600 + Math.sin(time * 0.35) * 220), 260);
    }

    private void drawStarsBadge(GraphicsContext gc) {
        double w = 236;
        double x = Draw.W - w - 32;
        double y = 30;
        Ui.panel(gc, x, y, w, 92);
        Draw.text(gc, "ÉTOILES", x + 18, y + 22, assets.ui(14), Draw.TEXT_DIM, TextAlignment.LEFT);
        Draw.text(gc, config.getAchievements().size() + " / " + Achievement.values().length, x + w - 18, y + 22,
                assets.ui(14), Draw.GOLD, TextAlignment.RIGHT);
        Achievement[] all = Achievement.values();
        for (int i = 0; i < all.length; i++) {
            Ui.star(gc, x + 40 + i * 52, y + 56, 15, config.isUnlocked(all[i]));
        }
        Draw.text(gc, "Nuit " + config.getUnlockedNight() + " / " + NightConfig.LAST_NIGHT + " débloquée",
                Draw.W - 32 - w / 2, y + 112, assets.ui(13), Draw.TEXT_DIM, TextAlignment.CENTER);
        if (!sound.isAvailable()) {
            Ui.warningIcon(gc, x + 8, y + 140, 7, Draw.AMBER);
            Draw.text(gc, "Aucune sortie audio : voir le README", x + 22, y + 140, assets.ui(13), Draw.AMBER,
                    TextAlignment.LEFT);
        }
    }

    private void drawFooter(GraphicsContext gc) {
        if (footer == null) {
            footer = Draw.render(Draw.W, 46, g -> {
                g.setFill(Color.rgb(0, 0, 0, 0.55));
                g.fillRect(0, 0, Draw.W, 46);
                g.setStroke(Color.rgb(201, 162, 74, 0.3));
                g.setLineWidth(1);
                g.strokeLine(0, 0.5, Draw.W, 0.5);
                Ui.hints(g, Draw.W / 2, 23, TextAlignment.CENTER, assets.ui(14), "Haut / Bas", "Choisir",
                        "Gauche / Droite", "Régler", "Entrée", "Valider", "M", "Son", "Échap", "Quitter");
            });
        }
        gc.drawImage(footer, 0, Draw.H - 46);

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

    private void changeVolume(int step) {
        sound.adjustVolume(step);
        try {
            config.save();
        } catch (java.io.IOException e) {
            System.err.println("Impossible d'enregistrer le volume : " + e.getMessage());
        }
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
        double cx = 940 + sway + (scaring ? Draw.random(-8, 8) : 0);
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
        Draw.spriteAt(gc, assets.sprite("bobo", "idle_2", 520), 760 + Draw.random(-3, 3),
                Math.round(Draw.H + 420 - 440 * rise));
        if (t > 0.15) {
            Draw.shadowText(gc, "HONK !", 780, 170, assets.title(84), Draw.GOLD, TextAlignment.CENTER);
        }
    }

    private void drawHelp(GraphicsContext gc) {
        Draw.shade(gc, 0.7);
        double x = 200;
        double y = 70;
        double w = Draw.W - 400;
        double h = Draw.H - 140;
        Ui.window(gc, x, y, w, h, "COMMENT JOUER", assets.title(36));
        for (int i = 0; i < RULES.length; i++) {
            Draw.text(gc, RULES[i], Draw.W / 2, y + 112 + i * 28, assets.ui(16), Draw.TEXT, TextAlignment.CENTER);
        }
        gc.setStroke(Color.rgb(201, 162, 74, 0.25));
        gc.setLineWidth(1);
        gc.strokeLine(x + 60, y + 200, x + w - 60, y + 200);
        for (int i = 0; i < HELP.length; i++) {
            double cy = y + 226 + i * 34;
            double kw = Ui.keycap(gc, HELP[i][0], x + 150, cy, assets.ui(16));
            Draw.text(gc, HELP[i][1], x + 150 + Math.max(kw, 90) + 24, cy, assets.ui(17), Draw.CREAM, TextAlignment.LEFT);
        }
        Ui.hints(gc, Draw.W / 2, y + h - 30, TextAlignment.CENTER, assets.ui(14), "Entrée", "Fermer");
    }

    private void drawStarsPanel(GraphicsContext gc) {
        Draw.shade(gc, 0.7);
        double x = 260;
        double y = 110;
        double w = Draw.W - 520;
        double h = Draw.H - 220;
        Ui.window(gc, x, y, w, h, "ÉTOILES", assets.title(36));
        Achievement[] all = Achievement.values();
        for (int i = 0; i < all.length; i++) {
            boolean lit = config.isUnlocked(all[i]);
            double rowY = y + 130 + i * 74;
            if (lit) {
                gc.setFill(Color.rgb(201, 162, 74, 0.08));
                gc.fillRoundRect(x + 40, rowY - 30, w - 80, 60, 10, 10);
            }
            Ui.star(gc, x + 86, rowY, 21, lit);
            Draw.text(gc, all[i].getTitle(), x + 132, rowY - 11, assets.title(22), lit ? Draw.GOLD : Draw.TEXT_DIM,
                    TextAlignment.LEFT);
            Draw.text(gc, all[i].getDescription(), x + 132, rowY + 15, assets.ui(16), lit ? Draw.TEXT : Draw.TEXT_DIM,
                    TextAlignment.LEFT);
            Draw.text(gc, lit ? "OBTENUE" : "À GAGNER", x + w - 60, rowY, assets.ui(13),
                    lit ? Draw.GOLD : Draw.TEXT_DIM, TextAlignment.RIGHT);
        }
        Ui.hints(gc, Draw.W / 2, y + h - 30, TextAlignment.CENTER, assets.ui(14), "Entrée", "Fermer");
    }

    private void drawCustomPanel(GraphicsContext gc) {
        Draw.shade(gc, 0.7);
        double x = 240;
        double y = 90;
        double w = Draw.W - 480;
        double h = Draw.H - 180;
        Ui.window(gc, x, y, w, h, "NUIT PERSONNALISÉE", assets.title(34));
        Draw.text(gc, "Réglez chaque clown de 0 (absent) à 20.", Draw.W / 2, y + 100, assets.ui(15),
                Draw.TEXT_DIM, TextAlignment.CENTER);
        customOptions.render(gc, assets.ui(21), assets.ui(23));

        // Portrait du clown choisi, sur un médaillon
        double px = x + w - 150;
        double py = y + h / 2 + 20;
        gc.setFill(new RadialGradient(0, 0, px, py, 140, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(120, 20, 30, 0.45)), new Stop(1, Color.TRANSPARENT)));
        gc.fillOval(px - 140, py - 140, 280, 280);
        int selected = customOptions.getSelected();
        if (selected < CustomNight.CLOWN_COUNT) {
            String frame = new String[]{"idle_1", "idle_2", "idle_3"}[(int) (time * 1.4) % 3];
            Draw.spriteAt(gc, assets.sprite(CLOWNS[selected], frame, 260), px, py + 110);
            Draw.text(gc, CLOWN_ROLES[selected], px, py + 132, assets.ui(14), Draw.TEXT_DIM, TextAlignment.CENTER);
            int level = custom.level(selected);
            Ui.gauge(gc, px - 90, py + 152, 180, 8, 20, level / 20.0, Ui.levelColor(1 - level / 20.0));
        } else {
            int total = 0;
            for (int i = 0; i < CustomNight.CLOWN_COUNT; i++) {
                total += custom.level(i);
            }
            Draw.shadowText(gc, total + " / 80", px, py - 10, assets.title(44),
                    custom.isMaxChallenge() ? Ui.ALERT : Draw.CREAM, TextAlignment.CENTER);
            Draw.text(gc, custom.isMaxChallenge() ? "Bonne chance." : "Difficulté totale", px, py + 36,
                    assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
        }
        Ui.hints(gc, Draw.W / 2, y + h - 30, TextAlignment.CENTER, assets.ui(14),
                "Gauche / Droite", "Régler", "Entrée", "Valider", "Échap", "Retour");
    }
}
