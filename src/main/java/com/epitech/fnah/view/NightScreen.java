package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.model.CameraSystem;
import com.epitech.fnah.model.Room;
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

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Écran de la nuit : la régie (porte, bureau, affiches) ou, tablette levée,
 * la caméra choisie avec son plan. Affiche aussi l'heure, l'énergie,
 * les sous-titres des bruits et l'introduction de chaque nuit.
 */
final class NightScreen implements Screen {

    private static final double W = Draw.W;
    private static final double H = Draw.H;

    // Porte gauche
    private static final double DOOR_X = 80, DOOR_Y = 110, DOOR_W = 270, DOOR_H = 480;
    // Bouton de la porte
    private static final double BTN_X = 372, BTN_Y = 300, BTN_W = 96, BTN_H = 140;
    // Barre « Caméras »
    private static final double BAR_X = 440, BAR_Y = 682, BAR_W = 400, BAR_H = 30;
    // Plan des caméras
    private static final double MAP_X = 950, MAP_Y = 410, MAP_W = 300, MAP_H = 270;
    private static final double CAM_W = 64, CAM_H = 28;
    private static final Map<Room, double[]> MAP_POSITIONS = new EnumMap<>(Map.of(
            Room.PISTE_CENTRALE, new double[]{118, 34},
            Room.GRADINS, new double[]{28, 80},
            Room.LOGES, new double[]{208, 80},
            Room.COULISSES_OUEST, new double[]{28, 130},
            Room.MENAGERIE, new double[]{208, 130},
            Room.COULOIR_GAUCHE, new double[]{28, 180},
            Room.COULOIR_DROIT, new double[]{208, 180},
            Room.FOSSE_ORCHESTRE, new double[]{118, 150},
            Room.REGIE, new double[]{118, 222}));
    // Bobo sur chaque caméra : centre x, ligne des pieds, hauteur de l'image
    private static final Map<Room, double[]> BOBO_ON_CAMERA = new EnumMap<>(Map.of(
            Room.PISTE_CENTRALE, new double[]{720, 650, 470},
            Room.GRADINS, new double[]{430, 660, 470},
            Room.COULISSES_OUEST, new double[]{820, 640, 430},
            Room.COULOIR_GAUCHE, new double[]{640, 610, 360}));
    // Pépin dort dans sa boîte, fosse d'orchestre (pas encore actif)
    private static final double[] PEPIN_ON_CAMERA = {640, 700, 430};

    private static final String[] NIGHT_LINES = {
        "Le propriétaire a été clair : ne laissez rien entrer.",
        "On raconte que la troupe n'est jamais repartie.",
        "Le 31 octobre 1994, les loges ont brûlé.",
        "Personne n'a retrouvé les clowns. Personne.",
        "Dernière nuit. Ils le savent aussi.",
    };
    private static final double INTRO_DURATION = 4.0;
    private static final double SWITCH_STATIC = 0.3;
    private static final double MOVE_STATIC = 1.0;
    private static final double SUBTITLE_DURATION = 3.0;
    private static final double FAST_TIME_SCALE = 10.0;

    private final Game game;
    private final Assets assets;
    private final Map<Room, Image> cameraBases = new EnumMap<>(Room.class);
    private Image officeBackground;
    private Image corridorView;

    private double time;
    private double intro;
    private double staticTimer;
    private double subtitleTimer;
    private String subtitle;
    private double flicker;
    private double timeScale = 1.0;

    NightScreen(Game game, Assets assets) {
        this.game = game;
        this.assets = assets;
        var events = game.getEvents();
        events.subscribe(GameEvent.CameraSelected.class, e -> staticTimer = SWITCH_STATIC);
        events.subscribe(GameEvent.TabletToggled.class, e -> {
            if (e.open()) {
                staticTimer = SWITCH_STATIC;
            }
        });
        events.subscribe(GameEvent.AnimatronicMoved.class, this::onAnimatronicMoved);
    }

    /**
     * Vitesse du temps : 1 en jeu normal, 10 en mode test (F2).
     *
     * @return multiplicateur appliqué au temps de jeu
     */
    double timeScale() {
        return timeScale;
    }

    @Override
    public void onEnter() {
        if (game.getClock().getElapsedSeconds() < 0.5) {
            intro = INTRO_DURATION;
            subtitleTimer = 0;
            staticTimer = 0;
            timeScale = 1.0;
        }
    }

    @Override
    public void update(double dt) {
        time += dt;
        intro = Math.max(0, intro - dt);
        staticTimer = Math.max(0, staticTimer - dt);
        subtitleTimer = Math.max(0, subtitleTimer - dt);
        if (Math.random() < 0.008) {
            flicker = Draw.random(0.25, 0.6);
        } else {
            flicker = Math.max(0, flicker - dt * 4);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (game.getOffice().getCameras().isOpen()) {
            drawCameraView(gc);
        } else {
            drawOffice(gc);
        }
        drawTabletBar(gc);
        drawHud(gc);
        if (intro > 0) {
            drawIntro(gc);
        }
    }

    @Override
    public void onKey(KeyEvent event) {
        KeyCode code = event.getCode();
        int camera = cameraNumber(event);
        if (code == KeyCode.Q) {
            game.toggleLeftDoor();
        } else if (code == KeyCode.SPACE) {
            game.toggleTablet();
        } else if (code == KeyCode.LEFT) {
            game.cycleCamera(-1);
        } else if (code == KeyCode.RIGHT) {
            game.cycleCamera(1);
        } else if (camera > 0) {
            game.selectCamera(camera);
        } else if (code == KeyCode.F2) {
            timeScale = timeScale > 1 ? 1.0 : FAST_TIME_SCALE;
        } else if (code == KeyCode.ESCAPE) {
            timeScale = 1.0;
            game.returnToMenu();
        }
    }

    @Override
    public void onClick(double x, double y) {
        boolean open = game.getOffice().getCameras().isOpen();
        if (Draw.inside(x, y, BAR_X, BAR_Y, BAR_W, BAR_H)) {
            game.toggleTablet();
        } else if (!open && Draw.inside(x, y, BTN_X, BTN_Y, BTN_W, BTN_H)) {
            game.toggleLeftDoor();
        } else if (open) {
            cameraAt(x, y).ifPresent(room -> game.selectCamera(room.getCameraNumber()));
        }
    }

    // ------------------------------------------------------------------ événements

    private void onAnimatronicMoved(GameEvent.AnimatronicMoved event) {
        CameraSystem cameras = game.getOffice().getCameras();
        if (cameras.isWatching(event.from()) || cameras.isWatching(event.to())) {
            staticTimer = MOVE_STATIC;
        }
        if (event.to() == Room.REGIE) {
            return;
        }
        int before = game.getMap().distance(event.from(), Room.REGIE);
        int after = game.getMap().distance(event.to(), Room.REGIE);
        if (after == 1) {
            showSubtitle("* Un klaxon, juste derrière la porte gauche *");
        } else if (after < before) {
            showSubtitle("* Un klaxon résonne sous le chapiteau, plus proche *");
        } else {
            showSubtitle("* Des pas lourds s'éloignent vers les gradins *");
        }
    }

    private void showSubtitle(String text) {
        subtitle = text;
        subtitleTimer = SUBTITLE_DURATION;
    }

    // ------------------------------------------------------------------ régie

    private void drawOffice(GraphicsContext gc) {
        if (officeBackground == null) {
            officeBackground = Draw.render(W, H, this::paintOfficeBackground);
        }
        gc.drawImage(officeBackground, 0, 0);
        drawDoor(gc);
        drawDoorButton(gc);
        Draw.grain(gc, 575, 444, 180, 100, 90, 0.35);

        Draw.grain(gc, 250, 0.10);
        Draw.overlay(gc, 0, 0, 0.92);
        if (flicker > 0) {
            Draw.shade(gc, flicker);
        }
    }

    /** Tout ce qui ne bouge jamais dans la régie, dessiné une seule fois. */
    private void paintOfficeBackground(GraphicsContext gc) {
        // Mur : papier peint rayé de cirque, délavé
        Draw.clear(gc, Color.web("#1a1012"));
        for (double x = 0; x < W; x += 64) {
            gc.setFill(Color.rgb(90, 20, 28, 0.22));
            gc.fillRect(x, 0, 32, 470);
        }
        gc.setFill(Color.web("#140b0c"));
        gc.fillRect(0, 470, W, 120);
        gc.setFill(Color.web("#3a2a1c"));
        gc.fillRect(0, 466, W, 6);
        gc.setFill(new LinearGradient(0, 590, 0, H, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#120c0b")), new Stop(1, Color.web("#050303"))));
        gc.fillRect(0, 590, W, H - 590);
        gc.setStroke(Color.rgb(0, 0, 0, 0.5));
        gc.setLineWidth(2);
        for (double x = -400; x < W + 400; x += 90) {
            gc.strokeLine(W / 2 + (x - W / 2) * 0.35, 590, x, H);
        }
        drawPoster(gc);
        drawClipping(gc);
        drawDesk(gc);
        // Lumière chaude de la lampe de bureau
        gc.setFill(new RadialGradient(0, 0, 470, 520, 420, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 190, 110, 0.20)), new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, 0, W, H);
    }

    private void drawDoor(GraphicsContext gc) {
        boolean closed = game.getOffice().getLeftDoor().isClosed();
        // Le couloir réel, vu à travers l'encadrement
        if (corridorView == null) {
            corridorView = Draw.render(DOOR_W, DOOR_H, this::paintCorridor);
        }
        gc.drawImage(corridorView, DOOR_X, DOOR_Y);

        if (closed) {
            drawShutter(gc);
        } else if (game.getBobo().isAtDoor()) {
            double breathe = Math.sin(time * 2.2) * 4;
            Draw.spriteAt(gc, assets.sprite("bobo", "door", 470), DOOR_X + DOOR_W / 2, DOOR_Y + DOOR_H + 20 + breathe);
            gc.setFill(Color.rgb(0, 0, 0, 0.28));
            gc.fillRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        }
        gc.setStroke(Color.web("#2e2326"));
        gc.setLineWidth(18);
        gc.strokeRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        gc.setStroke(Color.web("#4a3a2c"));
        gc.setLineWidth(3);
        gc.strokeRect(DOOR_X - 9, DOOR_Y - 9, DOOR_W + 18, DOOR_H + 18);
    }

    private void paintCorridor(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, DOOR_W, DOOR_H);
        Image corridor = assets.camera(Room.COULOIR_GAUCHE);
        if (corridor != null) {
            double sw = corridor.getWidth() * 0.28;
            double sh = sw * DOOR_H / DOOR_W;
            gc.drawImage(corridor, (corridor.getWidth() - sw) / 2, (corridor.getHeight() - sh) / 2, sw, sh,
                    0, 0, DOOR_W, DOOR_H);
        }
        gc.setFill(Color.rgb(0, 0, 0, 0.62));
        gc.fillRect(0, 0, DOOR_W, DOOR_H);
    }

    private void drawShutter(GraphicsContext gc) {
        gc.setFill(new LinearGradient(DOOR_X, 0, DOOR_X + DOOR_W, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2c2a2b")), new Stop(0.5, Color.web("#4a4648")), new Stop(1, Color.web("#2c2a2b"))));
        gc.fillRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        gc.setStroke(Color.rgb(0, 0, 0, 0.45));
        gc.setLineWidth(4);
        for (double y = DOOR_Y + 24; y < DOOR_Y + DOOR_H - 40; y += 30) {
            gc.strokeLine(DOOR_X, y, DOOR_X + DOOR_W, y);
        }
        // Bande de danger en bas du rideau
        gc.save();
        gc.beginPath();
        gc.rect(DOOR_X, DOOR_Y + DOOR_H - 40, DOOR_W, 40);
        gc.closePath();
        gc.clip();
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(DOOR_X, DOOR_Y + DOOR_H - 40, DOOR_W, 40);
        gc.setFill(Color.web("#b8901c"));
        for (double x = DOOR_X - 40; x < DOOR_X + DOOR_W + 40; x += 40) {
            gc.fillPolygon(new double[]{x, x + 20, x + 60, x + 40},
                    new double[]{DOOR_Y + DOOR_H, DOOR_Y + DOOR_H, DOOR_Y + DOOR_H - 40, DOOR_Y + DOOR_H - 40}, 4);
        }
        gc.restore();
    }

    private void drawDoorButton(GraphicsContext gc) {
        boolean closed = game.getOffice().getLeftDoor().isClosed();
        gc.setFill(Color.web("#2a2426"));
        gc.fillRect(BTN_X, BTN_Y, BTN_W, BTN_H);
        gc.setStroke(Color.web("#4a3f3a"));
        gc.setLineWidth(2);
        gc.strokeRect(BTN_X + 4, BTN_Y + 4, BTN_W - 8, BTN_H - 8);
        double cx = BTN_X + BTN_W / 2;
        if (closed) {
            gc.setFill(new RadialGradient(0, 0, cx, BTN_Y + 46, 60, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 60, 70, 0.55)), new Stop(1, Color.TRANSPARENT)));
            gc.fillRect(BTN_X - 30, BTN_Y - 20, BTN_W + 60, 140);
        }
        gc.setFill(closed ? Color.web("#e0303f") : Color.web("#4a1a1f"));
        gc.fillOval(cx - 28, BTN_Y + 18, 56, 56);
        gc.setStroke(Color.web("#120808"));
        gc.setLineWidth(3);
        gc.strokeOval(cx - 28, BTN_Y + 18, 56, 56);
        Draw.text(gc, "PORTE", cx, BTN_Y + 96, assets.ui(15), Draw.TEXT, TextAlignment.CENTER);
        Draw.text(gc, "[Q]", cx, BTN_Y + 118, assets.ui(13), Draw.TEXT_DIM, TextAlignment.CENTER);
    }

    private void drawPoster(GraphicsContext gc) {
        gc.save();
        gc.translate(1010, 260);
        gc.rotate(2.5);
        gc.setFill(Draw.RED_DARK);
        gc.fillRect(-125, -160, 250, 330);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(3);
        gc.strokeRect(-113, -148, 226, 306);
        Draw.text(gc, "CIRQUE", 0, -95, assets.title(40), Draw.GOLD, TextAlignment.CENTER);
        Draw.text(gc, "MALVAUX", 0, -50, assets.title(40), Draw.GOLD, TextAlignment.CENTER);
        Draw.sprite(gc, assets.sprite("bobo", "idle_1"), 0, 110, 150);
        Draw.text(gc, "DERNIÈRE REPRÉSENTATION", 0, 125, assets.ui(13), Draw.CREAM, TextAlignment.CENTER);
        Draw.text(gc, "31 OCTOBRE 1994", 0, 145, assets.ui(15), Draw.CREAM, TextAlignment.CENTER);
        gc.restore();
    }

    private void drawClipping(GraphicsContext gc) {
        gc.save();
        gc.translate(1090, 500);
        gc.rotate(-5);
        gc.setFill(Color.web("#b9b09a"));
        gc.fillRect(-95, -55, 190, 110);
        Draw.text(gc, "INCENDIE AU CIRQUE", 0, -30, assets.ui(14), Color.web("#1d1a16"), TextAlignment.CENTER);
        Draw.text(gc, "Les clowns de la troupe", 0, 0, assets.ui(11), Color.web("#3a342c"), TextAlignment.CENTER);
        Draw.text(gc, "restent introuvables.", 0, 16, assets.ui(11), Color.web("#3a342c"), TextAlignment.CENTER);
        Draw.text(gc, "1er novembre 1994", 0, 38, assets.ui(10), Color.web("#5a5246"), TextAlignment.CENTER);
        gc.restore();
    }

    private void drawDesk(GraphicsContext gc) {
        // Plateau
        gc.setFill(Color.web("#2a1d16"));
        gc.fillPolygon(new double[]{400, 900, 980, 320}, new double[]{560, 560, H, H}, 4);
        gc.setFill(Color.web("#4a3426"));
        gc.fillRect(400, 556, 500, 8);
        // Écran de contrôle cathodique
        gc.setFill(Color.web("#1c1a19"));
        gc.fillRect(560, 430, 210, 135);
        gc.setFill(Color.web("#0b120c"));
        gc.fillRect(575, 444, 180, 100);
        Draw.text(gc, "CAM -- REC", 665, 494, assets.ui(14), Color.rgb(120, 200, 120, 0.7), TextAlignment.CENTER);
        // Lampe de bureau
        gc.setFill(Color.web("#1a1414"));
        gc.fillRect(462, 520, 16, 40);
        gc.fillPolygon(new double[]{440, 500, 488, 452}, new double[]{520, 520, 490, 490}, 4);
        gc.setFill(Color.rgb(255, 210, 140, 0.9));
        gc.fillOval(458, 514, 24, 10);
        // Tasse
        gc.setFill(Color.web("#d8cfc0"));
        gc.fillRect(820, 530, 30, 30);
        gc.setFill(Color.web("#2a1a10"));
        gc.fillOval(820, 526, 30, 9);
    }

    // ------------------------------------------------------------------ caméras

    private void drawCameraView(GraphicsContext gc) {
        CameraSystem cameras = game.getOffice().getCameras();
        Room room = cameras.getSelected();
        Image base = cameraBases.computeIfAbsent(room, r -> Draw.render(W, H, g -> {
            g.setFill(Color.web("#101010"));
            g.fillRect(0, 0, W, H);
            Draw.cover(g, assets.camera(r), 0, 0, W, H, 1.0);
        }));
        gc.drawImage(base, 0, 0);

        if (game.getBobo().getRoom() == room) {
            double[] p = BOBO_ON_CAMERA.getOrDefault(room, new double[]{W / 2, 650, 430});
            String frame = new String[]{"idle_1", "idle_2", "idle_1", "idle_3"}[(int) (time * 1.3) % 4];
            Draw.spriteAt(gc, assets.sprite("bobo", frame, p[2]), p[0], p[1]);
        }
        if (room == Room.FOSSE_ORCHESTRE) {
            String frame = new String[]{"idle_1", "idle_2", "idle_3"}[(int) (time * 0.8) % 3];
            Draw.spriteAt(gc, assets.sprite("pepin", frame, PEPIN_ON_CAMERA[2]), PEPIN_ON_CAMERA[0], PEPIN_ON_CAMERA[1]);
        }

        Draw.overlay(gc, 0.3, 0.25, 0.85);
        boolean noisy = staticTimer > 0;
        Draw.grain(gc, noisy ? 2200 : 220, noisy ? 0.6 : 0.12);
        if (noisy) {
            gc.setFill(Color.rgb(160, 160, 160, 0.25 * Math.min(1.0, staticTimer / SWITCH_STATIC)));
            gc.fillRect(0, 0, W, H);
        }

        Draw.shadowText(gc, room.getCameraLabel() + " - " + room.getDisplayName().toUpperCase(),
                40, 50, assets.ui(28), Draw.CREAM, TextAlignment.LEFT);
        if (((int) (time * 2)) % 2 == 0) {
            gc.setFill(Draw.RED);
            gc.fillOval(42, 82, 14, 14);
        }
        Draw.text(gc, "REC", 64, 89, assets.ui(17), Draw.TEXT, TextAlignment.LEFT);
        Draw.text(gc, "[1-8] ou [Gauche / Droite] Changer de caméra", W / 2, 22, assets.ui(15),
                Draw.TEXT_DIM, TextAlignment.CENTER);
        drawCameraMap(gc, cameras);
    }

    private void drawCameraMap(GraphicsContext gc, CameraSystem cameras) {
        gc.setFill(Color.rgb(0, 0, 0, 0.65));
        gc.fillRect(MAP_X, MAP_Y, MAP_W, MAP_H);
        gc.setStroke(Draw.TEXT_DIM);
        gc.setLineWidth(1);
        gc.strokeRect(MAP_X, MAP_Y, MAP_W, MAP_H);
        gc.setStroke(Color.web("#5a524b"));
        gc.setLineWidth(3);
        for (Room room : Room.values()) {
            double[] a = MAP_POSITIONS.get(room);
            for (Room next : game.getMap().neighbors(room)) {
                if (room.ordinal() < next.ordinal()) {
                    double[] b = MAP_POSITIONS.get(next);
                    gc.strokeLine(MAP_X + a[0], MAP_Y + a[1], MAP_X + b[0], MAP_Y + b[1]);
                }
            }
        }
        for (Room room : Room.values()) {
            double[] p = MAP_POSITIONS.get(room);
            double x = MAP_X + p[0] - CAM_W / 2;
            double y = MAP_Y + p[1] - CAM_H / 2;
            boolean selected = room == cameras.getSelected();
            gc.setFill(room == Room.REGIE ? Color.web("#4a1a1f") : selected ? Draw.AMBER : Color.web("#2a2626"));
            gc.fillRect(x, y, CAM_W, CAM_H);
            gc.setStroke(selected ? Draw.CREAM : Draw.TEXT_DIM);
            gc.setLineWidth(selected ? 2 : 1);
            gc.strokeRect(x, y, CAM_W, CAM_H);
            String label = room == Room.REGIE ? "VOUS" : room.getCameraLabel();
            Draw.text(gc, label, x + CAM_W / 2, y + CAM_H / 2, assets.ui(13),
                    selected ? Color.BLACK : Draw.TEXT, TextAlignment.CENTER);
        }
    }

    private Optional<Room> cameraAt(double x, double y) {
        for (Room room : Room.withCameras()) {
            double[] p = MAP_POSITIONS.get(room);
            if (Draw.inside(x, y, MAP_X + p[0] - CAM_W / 2, MAP_Y + p[1] - CAM_H / 2, CAM_W, CAM_H)) {
                return Optional.of(room);
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------ interface

    private void drawTabletBar(GraphicsContext gc) {
        boolean open = game.getOffice().getCameras().isOpen();
        gc.setFill(Color.rgb(20, 18, 18, 0.88));
        gc.fillRect(BAR_X, BAR_Y, BAR_W, BAR_H);
        gc.setStroke(open ? Draw.AMBER : Draw.TEXT_DIM);
        gc.setLineWidth(1.5);
        gc.strokeRect(BAR_X, BAR_Y, BAR_W, BAR_H);
        String label = open ? "BAISSER LA TABLETTE  [Espace]" : "CAMÉRAS  [Espace]";
        Draw.text(gc, label, BAR_X + BAR_W / 2, BAR_Y + BAR_H / 2, assets.ui(16),
                open ? Draw.AMBER : Draw.TEXT, TextAlignment.CENTER);
    }

    private void drawHud(GraphicsContext gc) {
        // Heure et nuit
        Draw.shadowText(gc, game.getClock().getFormattedTime(), W - 40, 52, assets.title(48), Draw.CREAM,
                TextAlignment.RIGHT);
        Draw.shadowText(gc, "NUIT " + game.getCurrentNight(), W - 42, 96, assets.ui(20), Draw.TEXT,
                TextAlignment.RIGHT);

        // Énergie : pourcentage et batterie à 10 cases
        double energy = game.getPower().getEnergy();
        Color level = energy > 50 ? Color.web("#7fbf6a") : energy > 25 ? Draw.AMBER : Color.web("#e0303f");
        Draw.shadowText(gc, "ÉNERGIE", 40, 612, assets.ui(15), Draw.TEXT_DIM, TextAlignment.LEFT);
        Draw.shadowText(gc, (int) Math.ceil(energy) + " %", 40, 646, assets.title(34), level, TextAlignment.LEFT);
        int cells = (int) Math.ceil(energy / 10);
        for (int i = 0; i < 10; i++) {
            gc.setFill(i < cells ? level : Color.rgb(255, 255, 255, 0.08));
            gc.fillRect(40 + i * 20, 676, 16, 14);
        }
        Draw.text(gc, "USAGE", 260, 683, assets.ui(13), Draw.TEXT_DIM, TextAlignment.LEFT);
        int usage = game.getPower().computeUsage();
        for (int i = 0; i < 4; i++) {
            gc.setFill(i < usage ? (i == 0 ? Color.web("#7fbf6a") : i == 1 ? Draw.AMBER : Color.web("#e0303f"))
                    : Color.rgb(255, 255, 255, 0.08));
            gc.fillRect(318 + i * 14, 676, 10, 14);
        }

        if (timeScale > 1) {
            Draw.text(gc, "TEMPS x" + (int) timeScale + "  (F2)", 40, 132, assets.ui(16), Draw.AMBER, TextAlignment.LEFT);
        }

        if (subtitleTimer > 0 && subtitle != null) {
            gc.setGlobalAlpha(Math.min(1.0, subtitleTimer));
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.fillRect(W / 2 - 330, 606, 660, 36);
            Draw.text(gc, subtitle, W / 2, 624, assets.ui(19), Draw.CREAM, TextAlignment.CENTER);
            gc.setGlobalAlpha(1.0);
        }
    }

    private void drawIntro(GraphicsContext gc) {
        double alpha = Math.min(1.0, intro / 1.2);
        gc.setGlobalAlpha(alpha);
        Draw.clear(gc, Color.BLACK);
        int night = game.getCurrentNight();
        Draw.text(gc, "00:00", W / 2, H / 2 - 110, assets.ui(26), Draw.TEXT_DIM, TextAlignment.CENTER);
        Draw.text(gc, "NUIT " + night, W / 2, H / 2 - 30, assets.title(88), Draw.RED, TextAlignment.CENTER);
        Draw.text(gc, NIGHT_LINES[Math.min(night, NIGHT_LINES.length) - 1], W / 2, H / 2 + 60,
                assets.ui(20), Draw.TEXT, TextAlignment.CENTER);
        gc.setGlobalAlpha(1.0);
    }

    /** Caractères de la rangée des chiffres d'un clavier AZERTY, sans Maj (touches 1 à 8). */
    private static final String AZERTY_DIGITS = "&é\"'(-è_";

    /**
     * Numéro de caméra d'une touche, quel que soit le clavier : rangée de chiffres
     * (QWERTY, ou AZERTY avec ou sans Maj) et pavé numérique.
     */
    static int cameraNumber(KeyEvent event) {
        String name = event.getCode().name();
        if (name.startsWith("DIGIT") || name.startsWith("NUMPAD")) {
            char last = name.charAt(name.length() - 1);
            if (Character.isDigit(last)) {
                return last - '0';
            }
        }
        String text = event.getText();
        if (text != null && text.length() == 1) {
            char c = text.charAt(0);
            if (Character.isDigit(c)) {
                return c - '0';
            }
            int index = AZERTY_DIGITS.indexOf(c);
            if (index >= 0) {
                return index + 1;
            }
        }
        return -1;
    }
}
