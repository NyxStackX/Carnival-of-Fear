package com.epitech.fnah.view;

import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.config.NightConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameLoop;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.model.CameraSystem;
import com.epitech.fnah.model.Room;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Dessine chaque écran du jeu sur le canvas. Ne modifie jamais la logique :
 * il lit l'état de {@link Game} et s'abonne à l'{@link com.epitech.fnah.core.EventBus}
 * pour les sous-titres, le grésillement des caméras et le jumpscare.
 *
 * <p>Les vues caméras utilisent les images de {@link CameraImages}. Le reste
 * (régie, Bobo) est dessiné avec des formes simples en attendant les sprites.</p>
 */
public final class GameRenderer {

    private static final double W = GameApp.WIDTH;
    private static final double H = GameApp.HEIGHT;

    private static final Color WALL = Color.web("#1b1416");
    private static final Color FLOOR = Color.web("#120d0e");
    private static final Color DARK = Color.web("#050404");
    private static final Color FRAME = Color.web("#2e2326");
    private static final Color SHUTTER = Color.web("#3b3638");
    private static final Color SHUTTER_RIB = Color.web("#2a2628");
    private static final Color RED = Color.web("#b3202f");
    private static final Color RED_DIM = Color.web("#4a1a1f");
    private static final Color CREAM = Color.web("#e8e0d0");
    private static final Color AMBER = Color.web("#ffb000");
    private static final Color TEXT = Color.web("#d8cfc0");
    private static final Color TEXT_DIM = Color.web("#8a8178");

    // Porte gauche (ouverture dans le mur)
    private static final double DOOR_X = 70, DOOR_Y = 120, DOOR_W = 260, DOOR_H = 470;
    // Bouton de la porte gauche
    private static final double BTN_X = 350, BTN_Y = 320, BTN_W = 90, BTN_H = 120;

    // Barre « Caméras » en bas de l'écran (lever / baisser la tablette)
    private static final double BAR_X = 440, BAR_Y = 682, BAR_W = 400, BAR_H = 30;
    // Plan des caméras (coin bas droit, tablette levée)
    private static final double MAP_X = 950, MAP_Y = 420, MAP_W = 300, MAP_H = 270;
    private static final double CAM_BTN_W = 64, CAM_BTN_H = 28;
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
    // Position et taille de Bobo dans chaque vue caméra : x, y du visage, échelle
    private static final Map<Room, double[]> BOBO_ON_CAMERA = new EnumMap<>(Map.of(
            Room.PISTE_CENTRALE, new double[]{720, 330, 0.75},
            Room.GRADINS, new double[]{420, 300, 0.7},
            Room.COULISSES_OUEST, new double[]{820, 300, 0.7},
            Room.COULOIR_GAUCHE, new double[]{640, 320, 0.6}));

    private static final double SWITCH_STATIC = 0.3;
    private static final double MOVE_STATIC = 1.0;
    private static final double SUBTITLE_DURATION = 3.0;
    private static final String FONT = "Arial";

    private final GraphicsContext gc;
    private final Game game;
    private final GameConfig config;
    private final CameraImages images;
    private final Random shake = new Random();
    private final Random noise = new Random();

    private String subtitle;
    private double subtitleTimer;
    private double jumpscareTime;
    private double staticTimer;
    private double clock;

    /**
     * Crée le moteur d'affichage et l'abonne aux événements de la partie.
     *
     * @param gc     contexte graphique du canvas
     * @param game   partie à afficher
     * @param config progression du joueur (nuits débloquées, affichées dans le menu)
     */
    public GameRenderer(GraphicsContext gc, Game game, GameConfig config) {
        this.gc = gc;
        this.game = game;
        this.config = config;
        this.images = CameraImages.load();
        game.getEvents().subscribe(GameEvent.AnimatronicMoved.class, this::onAnimatronicMoved);
        game.getEvents().subscribe(GameEvent.StateChanged.class, e -> resetEffects());
        game.getEvents().subscribe(GameEvent.CameraSelected.class, e -> staticTimer = SWITCH_STATIC);
        game.getEvents().subscribe(GameEvent.TabletToggled.class, e -> {
            if (e.open()) {
                staticTimer = SWITCH_STATIC;
            }
        });
    }

    /**
     * Fait avancer les animations propres à l'affichage (sous-titres, jumpscare).
     *
     * @param dt temps écoulé, pour les animations de l'affichage
     */
    public void update(double dt) {
        clock += dt;
        if (subtitleTimer > 0) {
            subtitleTimer -= dt;
        }
        if (staticTimer > 0) {
            staticTimer -= dt;
        }
        if (game.getState() == GameState.JUMPSCARE) {
            jumpscareTime += dt;
        }
    }

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
            showSubtitle("Un klaxon, tout près de la porte gauche...");
        } else if (after < before) {
            showSubtitle("Un klaxon résonne sous le chapiteau, plus proche.");
        } else {
            showSubtitle("Des pas lourds s'éloignent vers les gradins.");
        }
    }

    private void resetEffects() {
        subtitleTimer = 0;
        jumpscareTime = 0;
        staticTimer = 0;
    }

    /**
     * Indique si un clic tombe sur le bouton de la porte gauche.
     *
     * @param x abscisse du clic
     * @param y ordonnée du clic
     * @return {@code true} si le clic tombe sur le bouton de la porte gauche
     */
    public boolean isOnLeftDoorButton(double x, double y) {
        return !game.getOffice().getCameras().isOpen() && inside(x, y, BTN_X, BTN_Y, BTN_W, BTN_H);
    }

    /**
     * Indique si un clic tombe sur la barre qui lève ou baisse la tablette.
     *
     * @param x abscisse du clic
     * @param y ordonnée du clic
     * @return {@code true} si le clic tombe sur la barre « Caméras »
     */
    public boolean isOnTabletBar(double x, double y) {
        return inside(x, y, BAR_X, BAR_Y, BAR_W, BAR_H);
    }

    /**
     * Indique quelle caméra du plan se trouve sous un clic (tablette levée uniquement).
     *
     * @param x abscisse du clic
     * @param y ordonnée du clic
     * @return la salle cliquée, ou vide si le clic ne tombe sur aucune caméra
     */
    public Optional<Room> cameraAt(double x, double y) {
        if (!game.getOffice().getCameras().isOpen()) {
            return Optional.empty();
        }
        for (Room room : Room.withCameras()) {
            double[] p = MAP_POSITIONS.get(room);
            if (inside(x, y, MAP_X + p[0] - CAM_BTN_W / 2, MAP_Y + p[1] - CAM_BTN_H / 2, CAM_BTN_W, CAM_BTN_H)) {
                return Optional.of(room);
            }
        }
        return Optional.empty();
    }

    /**
     * Dessine l'écran correspondant à l'état du jeu.
     *
     * @param timeScale vitesse du temps (affichée si accélérée)
     */
    public void render(double timeScale) {
        switch (game.getState()) {
            case MENU, NIGHT_INTRO -> drawMenu();
            case PLAYING -> {
                if (game.getOffice().getCameras().isOpen()) {
                    drawCameraView();
                } else {
                    drawOffice();
                }
                drawTabletBar();
                drawHud(timeScale);
            }
            case JUMPSCARE -> drawJumpscare();
            case GAME_OVER -> drawGameOver();
            case WIN -> drawWin();
        }
    }

    // ------------------------------------------------------------------ écrans

    private void drawMenu() {
        clear(Color.BLACK);
        text("MINUIT AU CIRQUE MALVAUX", W / 2, 250, 56, FontWeight.BOLD, RED);
        int night = game.getCurrentNight();
        String left = night > 1 ? "<   " : "    ";
        String right = night < config.getUnlockedNight() ? "   >" : "    ";
        text(left + "Nuit " + night + right, W / 2, 330, 28, FontWeight.NORMAL, TEXT);
        text("Nuits débloquées : " + config.getUnlockedNight() + " / " + NightConfig.LAST_NIGHT,
                W / 2, 370, 16, FontWeight.NORMAL, TEXT_DIM);
        text("[Entrée] Commencer     [Gauche / Droite] Choisir la nuit     [Échap] Quitter",
                W / 2, 470, 20, FontWeight.NORMAL, TEXT_DIM);
        text("Suivez Bobo sur les caméras [Espace], fermez la porte gauche [Q] quand il approche.",
                W / 2, 530, 18, FontWeight.NORMAL, TEXT_DIM);
        text("Chaque équipement consomme de l'énergie.", W / 2, 560, 18, FontWeight.NORMAL, TEXT_DIM);
        text("[M] Couper / remettre le son", W / 2, 620, 16, FontWeight.NORMAL, TEXT_DIM);
    }

    private void drawOffice() {
        clear(WALL);

        gc.setFill(FLOOR);
        gc.fillRect(0, 590, W, H - 590);

        // Porte gauche : l'ouverture, Bobo s'il est là, ou le rideau métallique
        gc.setFill(DARK);
        gc.fillRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        boolean doorClosed = game.getOffice().getLeftDoor().isClosed();
        if (doorClosed) {
            drawShutter();
        } else if (game.getBobo().isAtDoor()) {
            drawClown(DOOR_X + DOOR_W / 2, DOOR_Y + 170, 1.0);
            gc.setFill(Color.rgb(0, 0, 0, 0.35));
            gc.fillRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        }
        gc.setStroke(FRAME);
        gc.setLineWidth(14);
        gc.strokeRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);

        drawDoorButton(doorClosed);

        // Affiche de cirque au mur
        gc.setFill(Color.web("#5a1019"));
        gc.fillRect(900, 150, 220, 300);
        gc.setStroke(Color.web("#c9a24a"));
        gc.setLineWidth(4);
        gc.strokeRect(910, 160, 200, 280);
        text("CIRQUE", 1010, 250, 30, FontWeight.BOLD, Color.web("#c9a24a"));
        text("MALVAUX", 1010, 290, 30, FontWeight.BOLD, Color.web("#c9a24a"));
        text("31.10.1994", 1010, 380, 18, FontWeight.NORMAL, CREAM);

        // Bureau
        gc.setFill(Color.web("#2a1f1a"));
        gc.fillRect(420, 560, 520, H - 560);
        gc.setFill(Color.web("#3a2c25"));
        gc.fillRect(420, 560, 520, 14);

        drawVignette();
    }

    private void drawCameraView() {
        CameraSystem cameras = game.getOffice().getCameras();
        Room room = cameras.getSelected();
        Image image = images.get(room);
        if (image != null) {
            gc.drawImage(image, 0, 0, W, H);
        } else {
            clear(Color.web("#101010"));
            text("Image indisponible", W / 2, H / 2, 24, FontWeight.NORMAL, TEXT_DIM);
        }

        if (game.getBobo().getRoom() == room) {
            double[] p = BOBO_ON_CAMERA.getOrDefault(room, new double[]{W / 2, 300, 0.7});
            drawClown(p[0], p[1], p[2]);
        }

        // Image de surveillance : assombrie, lignes de balayage, grain
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRect(0, 0, W, H);
        gc.setStroke(Color.rgb(0, 0, 0, 0.25));
        gc.setLineWidth(1);
        for (double y = 0; y < H; y += 4) {
            gc.strokeLine(0, y, W, y);
        }
        drawNoise(staticTimer > 0 ? 2500 : 250, staticTimer > 0 ? 0.55 : 0.12);
        if (staticTimer > 0) {
            gc.setFill(Color.rgb(160, 160, 160, 0.25 * Math.min(1.0, staticTimer / SWITCH_STATIC)));
            gc.fillRect(0, 0, W, H);
        }
        drawVignette();

        // Libellés de la caméra
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(CREAM);
        gc.setFont(Font.font(FONT, FontWeight.BOLD, 26));
        gc.fillText(room.getCameraLabel() + " - " + room.getDisplayName(), 40, 50);
        if (((int) (clock * 2)) % 2 == 0) {
            gc.setFill(RED);
            gc.fillOval(42, 80, 14, 14);
        }
        gc.setFill(TEXT);
        gc.setFont(Font.font(FONT, FontWeight.BOLD, 16));
        gc.fillText("REC", 64, 87);
        text("[1-8] ou [Gauche / Droite] Changer de caméra", W / 2, 22, 15, FontWeight.NORMAL, TEXT_DIM);

        drawCameraMap(cameras);
    }

    private void drawCameraMap(CameraSystem cameras) {
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(MAP_X, MAP_Y, MAP_W, MAP_H);
        gc.setStroke(TEXT_DIM);
        gc.setLineWidth(1);
        gc.strokeRect(MAP_X, MAP_Y, MAP_W, MAP_H);

        // Passages entre salles, lus directement dans le graphe du chapiteau
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
            double x = MAP_X + p[0] - CAM_BTN_W / 2;
            double y = MAP_Y + p[1] - CAM_BTN_H / 2;
            boolean selected = room == cameras.getSelected();
            gc.setFill(room == Room.REGIE ? RED_DIM : selected ? AMBER : Color.web("#2a2626"));
            gc.fillRect(x, y, CAM_BTN_W, CAM_BTN_H);
            gc.setStroke(selected ? CREAM : TEXT_DIM);
            gc.setLineWidth(selected ? 2 : 1);
            gc.strokeRect(x, y, CAM_BTN_W, CAM_BTN_H);
            String label = room == Room.REGIE ? "VOUS" : room.getCameraLabel();
            text(label, x + CAM_BTN_W / 2, y + CAM_BTN_H / 2, 13, FontWeight.BOLD, selected ? Color.BLACK : TEXT);
        }
    }

    private void drawTabletBar() {
        boolean open = game.getOffice().getCameras().isOpen();
        gc.setFill(Color.rgb(20, 18, 18, 0.85));
        gc.fillRect(BAR_X, BAR_Y, BAR_W, BAR_H);
        gc.setStroke(open ? AMBER : TEXT_DIM);
        gc.setLineWidth(1.5);
        gc.strokeRect(BAR_X, BAR_Y, BAR_W, BAR_H);
        String label = open ? "BAISSER LA TABLETTE  [Espace]" : "CAMÉRAS  [Espace]";
        text(label, BAR_X + BAR_W / 2, BAR_Y + BAR_H / 2, 15, FontWeight.BOLD, open ? AMBER : TEXT);
    }

    private void drawNoise(int dots, double maxAlpha) {
        for (int i = 0; i < dots; i++) {
            double gray = noise.nextDouble();
            gc.setFill(Color.gray(gray, noise.nextDouble() * maxAlpha));
            gc.fillRect(noise.nextDouble() * W, noise.nextDouble() * H, 3, 3);
        }
    }

    private void drawShutter() {
        gc.setFill(SHUTTER);
        gc.fillRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        gc.setStroke(SHUTTER_RIB);
        gc.setLineWidth(4);
        for (double y = DOOR_Y + 24; y < DOOR_Y + DOOR_H; y += 32) {
            gc.strokeLine(DOOR_X, y, DOOR_X + DOOR_W, y);
        }
    }

    private void drawDoorButton(boolean doorClosed) {
        gc.setFill(Color.web("#2a2224"));
        gc.fillRect(BTN_X, BTN_Y, BTN_W, BTN_H);
        gc.setFill(doorClosed ? RED : RED_DIM);
        gc.fillOval(BTN_X + BTN_W / 2 - 26, BTN_Y + 18, 52, 52);
        text("PORTE", BTN_X + BTN_W / 2, BTN_Y + 88, 14, FontWeight.BOLD, TEXT);
        text("[Q]", BTN_X + BTN_W / 2, BTN_Y + 106, 12, FontWeight.NORMAL, TEXT_DIM);
    }

    private void drawHud(double timeScale) {
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(TEXT);
        gc.setFont(Font.font(FONT, FontWeight.BOLD, 40));
        gc.fillText(game.getClock().getFormattedTime(), W - 40, 55);
        gc.setFont(Font.font(FONT, FontWeight.NORMAL, 20));
        gc.fillText("Nuit " + game.getCurrentNight(), W - 40, 95);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font(FONT, FontWeight.BOLD, 22));
        gc.fillText(String.format("Énergie : %d %%", (int) Math.ceil(game.getPower().getEnergy())), 40, 640);
        gc.setFont(Font.font(FONT, FontWeight.NORMAL, 18));
        gc.fillText("Usage", 40, 680);
        int usage = game.getPower().computeUsage();
        for (int i = 0; i < usage; i++) {
            gc.setFill(i == 0 ? Color.web("#4caf50") : i == 1 ? Color.web("#d4b400") : RED);
            gc.fillRect(105 + i * 26, 669, 20, 22);
        }

        if (timeScale > 1) {
            gc.setFill(AMBER);
            gc.setFont(Font.font(FONT, FontWeight.BOLD, 18));
            gc.fillText(String.format("Temps x%d (F2)", (int) timeScale), 40, 40);
        }

        if (subtitleTimer > 0 && subtitle != null) {
            gc.setGlobalAlpha(Math.min(1.0, subtitleTimer));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(CREAM);
            gc.setFont(Font.font(FONT, FontWeight.NORMAL, FontPosture.ITALIC, 22));
            gc.fillText(subtitle, W / 2, 640);
            gc.setGlobalAlpha(1.0);
        }
    }

    private void drawJumpscare() {
        boolean flash = ((int) (jumpscareTime * 12)) % 2 == 0;
        clear(flash ? Color.web("#3d0008") : Color.BLACK);
        double progress = Math.min(1.0, jumpscareTime / GameLoop.JUMPSCARE_DURATION);
        gc.save();
        gc.translate(shake.nextDouble(-14, 14), shake.nextDouble(-14, 14));
        drawClown(W / 2, 250, 2.0 + progress);
        gc.restore();
    }

    private void drawGameOver() {
        clear(Color.BLACK);
        text("NUIT PERDUE", W / 2, 260, 60, FontWeight.BOLD, RED);
        String attacker = game.getAttackerName();
        String reason = attacker != null ? attacker + " est entré dans la régie." : "Panne de courant.";
        text(reason, W / 2, 340, 24, FontWeight.NORMAL, TEXT);
        text("[Entrée] Rejouer        [Échap] Menu", W / 2, 470, 22, FontWeight.NORMAL, TEXT_DIM);
    }

    private void drawWin() {
        clear(Color.BLACK);
        text("06:00", W / 2, 250, 72, FontWeight.BOLD, CREAM);
        text("Nuit " + game.getCurrentNight() + " terminée", W / 2, 340, 28, FontWeight.NORMAL, TEXT);
        String hint = game.hasNextNight()
                ? "[Entrée] Nuit suivante        [Échap] Menu"
                : "Toutes les nuits sont terminées.        [Entrée] Menu";
        text(hint, W / 2, 470, 22, FontWeight.NORMAL, TEXT_DIM);
    }

    // ------------------------------------------------------------------ outils

    /**
     * Dessine Bobo, le clown triste, centré sur (cx, cy) au niveau du visage.
     */
    private void drawClown(double cx, double cy, double s) {
        // Manteau
        gc.setFill(Color.web("#2a1630"));
        gc.fillPolygon(
                new double[]{cx - 60 * s, cx + 60 * s, cx + 95 * s, cx - 95 * s},
                new double[]{cy + 70 * s, cy + 70 * s, cy + 290 * s, cy + 290 * s}, 4);
        // Collerette
        gc.setFill(Color.web("#cfc5b4"));
        for (int i = -3; i <= 3; i++) {
            gc.fillOval(cx + i * 22 * s - 16 * s, cy + 52 * s, 32 * s, 26 * s);
        }
        // Visage
        gc.setFill(CREAM);
        gc.fillOval(cx - 50 * s, cy - 60 * s, 100 * s, 125 * s);
        // Chapeau pointu
        gc.setFill(Color.web("#6b1420"));
        gc.fillPolygon(
                new double[]{cx - 38 * s, cx + 38 * s, cx + 6 * s},
                new double[]{cy - 48 * s, cy - 48 * s, cy - 150 * s}, 3);
        gc.setFill(CREAM);
        gc.fillOval(cx - 4 * s, cy - 162 * s, 20 * s, 20 * s);
        // Yeux
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 32 * s, cy - 28 * s, 22 * s, 26 * s);
        gc.fillOval(cx + 10 * s, cy - 28 * s, 22 * s, 26 * s);
        gc.setFill(AMBER);
        gc.fillOval(cx - 25 * s, cy - 19 * s, 8 * s, 8 * s);
        gc.fillOval(cx + 17 * s, cy - 19 * s, 8 * s, 8 * s);
        // Larme peinte
        gc.setStroke(Color.web("#1d2a4a"));
        gc.setLineWidth(4 * s);
        gc.strokeLine(cx - 21 * s, cy - 2 * s, cx - 24 * s, cy + 22 * s);
        // Nez
        gc.setFill(RED);
        gc.fillOval(cx - 12 * s, cy + 2 * s, 24 * s, 24 * s);
        // Bouche triste
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(4 * s);
        gc.strokeArc(cx - 26 * s, cy + 34 * s, 52 * s, 28 * s, 20, 140, ArcType.OPEN);
    }

    private void drawVignette() {
        gc.setFill(new RadialGradient(0, 0, W / 2, H / 2, W * 0.75, false, CycleMethod.NO_CYCLE,
                new Stop(0.45, Color.TRANSPARENT), new Stop(1.0, Color.rgb(0, 0, 0, 0.85))));
        gc.fillRect(0, 0, W, H);
    }

    private static boolean inside(double x, double y, double rx, double ry, double rw, double rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private void showSubtitle(String text) {
        subtitle = text;
        subtitleTimer = SUBTITLE_DURATION;
    }

    private void clear(Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, W, H);
    }

    private void text(String value, double x, double y, double size, FontWeight weight, Color color) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font(FONT, weight, size));
        gc.setFill(color);
        gc.fillText(value, x, y);
    }
}
