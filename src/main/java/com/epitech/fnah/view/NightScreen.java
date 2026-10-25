package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.entity.Mimi;
import com.epitech.fnah.entity.Pepin;
import com.epitech.fnah.entity.ZigEtZag;
import com.epitech.fnah.model.BarrelOrgan;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Écran de la nuit : la régie (portes, lampes, bureau, affiches) ou, tablette
 * levée, la caméra choisie avec son plan. Affiche aussi l'heure, l'énergie,
 * les sous-titres des bruits, la cassette du veilleur en début de nuit, l'orage,
 * les hallucinations, la coupure de courant et les easter eggs.
 *
 * <p>Tout ce qui est ici est de l'affichage : les règles du jeu restent dans
 * la logique, que cet écran pilote par {@link Game}.</p>
 */
final class NightScreen implements Screen {

    private static final double W = Draw.W;
    private static final double H = Draw.H;

    // Portes
    private static final double DOOR_X = 80, DOOR_Y = 110, DOOR_W = 270, DOOR_H = 480;
    private static final double RIGHT_DOOR_X = 930, RIGHT_DOOR_Y = 110, RIGHT_DOOR_W = 270, RIGHT_DOOR_H = 480;
    // Boutons des portes et des lampes
    private static final double BTN_X = 372, BTN_Y = 300, BTN_W = 96, BTN_H = 140;
    private static final double RIGHT_BTN_X = 812, RIGHT_BTN_Y = 300, RIGHT_BTN_W = 96, RIGHT_BTN_H = 140;
    private static final double LIGHT_Y = 222, LIGHT_H = 66;
    // Barre « Caméras » et pause
    private static final double BAR_X = 440, BAR_Y = 682, BAR_W = 400, BAR_H = 30;
    private static final double PAUSE_X = 860, PAUSE_Y = 682, PAUSE_W = 160, PAUSE_H = 30;
    private static final double PAUSE_PANEL_X = 390, PAUSE_PANEL_Y = 210, PAUSE_PANEL_W = 500, PAUSE_PANEL_H = 300;
    private static final double RESUME_X = 490, RESUME_Y = 365, PAUSE_ACTION_W = 300, PAUSE_ACTION_H = 52;
    private static final double MENU_Y = 435;
    // Plan des caméras
    private static final double MAP_X = 950, MAP_Y = 410, MAP_W = 300, MAP_H = 270;
    private static final double CAM_W = 64, CAM_H = 28;
    // Manivelle de l'orgue (caméra 8)
    private static final double ORGAN_X = 40, ORGAN_Y = 430, ORGAN_W = 340, ORGAN_H = 140;
    // Nez de Bobo sur l'affiche (coordonnées écran) : easter egg
    private static final double NOSE_X = 640, NOSE_Y = 246, NOSE_R = 15;
    // Coupure de journal punaisée au mur, entre l'affiche et la porte droite
    private static final double CLIP_X = 845, CLIP_Y = 150;
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
    // Pépin dans sa boîte, fosse d'orchestre
    private static final double[] PEPIN_ON_CAMERA = {640, 700, 430};

    private static final String[] NIGHT_LINES = {
        "Le propriétaire a été clair : ne laissez rien entrer.",
        "On raconte que la troupe n'est jamais repartie.",
        "Le 31 octobre 1994, les loges ont brûlé.",
        "Personne n'a retrouvé les clowns. Personne.",
        "Dernière nuit. Ils le savent aussi.",
    };
    /** Cassettes laissées par l'ancien veilleur, une par nuit. */
    private static final String[] CASSETTES = {
        "Allô ? Bon. Si vous écoutez ça, c'est que la famille Malvaux vous a confié la régie. "
            + "Moi, c'est Marcel, le veilleur d'avant. Les règles sont simples : surveillez les caméras, "
            + "gardez un oeil sur l'énergie, et fermez une porte quand quelqu'un est juste derrière. "
            + "Les lampes des couloirs vous montreront qui. Et... ne vous attachez pas aux clowns.",
        "Deuxième nuit. Vous êtes encore là, tant mieux. Je vous dois la vérité sur la troupe. "
            + "Le 31 octobre 1994, pendant la dernière représentation, le feu a pris dans les loges. "
            + "Le public est sorti. Les clowns, eux, n'ont jamais été retrouvés. "
            + "Mimi était la plus jeune. Elle passe encore par le couloir droit.",
        "Si vous voyez des choses qui ne devraient pas être là... l'affiche qui bouge, des mots sur "
            + "les écrans... ne paniquez pas. Ça m'est arrivé aussi. Le chapiteau vous teste. "
            + "Et pensez à l'orgue de barbarie, caméra 8. Pépin dort tant que la musique joue. "
            + "Remontez-le. Toujours.",
        "Je n'ai jamais dit qui avait mis le feu. C'était Henry, le directeur. Les clowns voulaient "
            + "partir, il a fermé les portes de l'intérieur. Depuis, quand le courant tombe, c'est lui "
            + "qui vient jouer sa mélodie. S'il ne vous reste rien, priez pour que six heures sonnent "
            + "avant la fin de la chanson.",
        "C'est ma dernière cassette. Demain, je ne serai plus là pour en enregistrer. "
            + "Ils ne veulent pas vous faire peur, vous savez. Ils veulent juste que quelqu'un reste "
            + "avec eux. Pour toujours. Comme moi. Tenez jusqu'à six heures. Et après... ne revenez pas.",
    };
    private static final double TITLE_DURATION = 3.2;
    private static final double TYPE_SPEED = 30;
    private static final double CASSETTE_LINGER = 3.0;
    private static final int CASSETTE_LINE = 62;
    private static final double SWITCH_STATIC = 0.3;
    private static final double MOVE_STATIC = 1.0;
    private static final double SUBTITLE_DURATION = 3.0;
    private static final double FAST_TIME_SCALE = 10.0;
    private static final double GOLDEN_CHANCE = 0.001;
    private static final double GOLDEN_STARE = 2.5;
    private static final double CRASH_DURATION = 5.0;
    private static final double LIGHTNING_DURATION = 0.5;
    private static final double ORGAN_WARNING = 40;

    /** Hallucinations possibles à partir de la nuit 3. */
    private enum Hallucination { NONE, POSTER, CLIPPING, CAMERA_TEXT }

    private final Game game;
    private final Assets assets;
    private final SoundManager sound;
    private final Map<Room, Image> cameraBases = new EnumMap<>(Room.class);
    private final Set<Integer> cassettesHeard = new HashSet<>();
    private Image officeBackground;
    private Image leftDark;
    private Image leftLit;
    private Image rightDark;
    private Image rightLit;

    private double time;
    private double titleCard;
    private boolean cassetteActive;
    private double cassetteTime;
    private int typedChars;
    private List<String> cassetteLines = List.of();
    private double staticTimer;
    private double subtitleTimer;
    private String subtitle;
    private double flicker;
    private double timeScale = 1.0;
    private boolean leftLightFlicker;
    private boolean rightLightFlicker;
    // Battement de cœur
    private double beatTimer;
    private double pulse;
    // Hallucinations
    private Hallucination hallucination = Hallucination.NONE;
    private double hallucinationTimer;
    private double nextHallucination;
    // Orage
    private double lightning;
    private double nextLightning;
    private double thunderDelay;
    private boolean silhouette;
    // Easter eggs
    private Room goldenRoom;
    private double goldenStare;
    private double crashTimer;
    private double noseTimer;

    NightScreen(Game game, Assets assets, SoundManager sound) {
        this.game = game;
        this.assets = assets;
        this.sound = sound;
        var events = game.getEvents();
        events.subscribe(GameEvent.CameraSelected.class, e -> onCameraShown(e.camera()));
        events.subscribe(GameEvent.TabletToggled.class, e -> {
            if (e.open()) {
                onCameraShown(e.camera());
            }
        });
        events.subscribe(GameEvent.AnimatronicMoved.class, this::onAnimatronicMoved);
        events.subscribe(GameEvent.PowerOut.class, e -> showSubtitle("* Un claquement. Puis plus rien. *"));
        events.subscribe(GameEvent.PepinEscaped.class, e -> showSubtitle(e.out()
                ? "* La musique s'arrête. Un ressort grince sous la trappe... *"
                : "* La boîte se referme dans la fosse d'orchestre *"));
    }

    /**
     * Vitesse du temps de jeu : 0 pendant l'introduction, la cassette et le faux
     * plantage, 10 en mode test (F2), 1 sinon.
     *
     * @return multiplicateur appliqué au temps de jeu
     */
    double timeScale() {
        return isFrozen() ? 0.0 : timeScale;
    }

    private boolean isFrozen() {
        return titleCard > 0 || cassetteActive || crashTimer > 0;
    }

    @Override
    public void onEnter() {
        if (game.getClock().getElapsedSeconds() < 0.5) {
            int night = game.getCurrentNight();
            boolean story = !game.isCustomNight();
            titleCard = TITLE_DURATION;
            cassetteActive = story && cassettesHeard.add(night);
            cassetteLines = wrap(CASSETTES[Math.min(night, CASSETTES.length) - 1], CASSETTE_LINE);
            cassetteTime = 0;
            typedChars = 0;
            subtitleTimer = 0;
            staticTimer = 0;
            timeScale = 1.0;
            pulse = 0;
            beatTimer = 0;
            hallucination = Hallucination.NONE;
            nextHallucination = Draw.random(15, 30);
            lightning = 0;
            nextLightning = Draw.random(8, 18);
            thunderDelay = 0;
            goldenRoom = null;
            crashTimer = 0;
        }
    }

    // ------------------------------------------------------------------ mise à jour

    @Override
    public void update(double dt) {
        time += dt;
        if (crashTimer > 0) {
            crashTimer -= dt;
            if (crashTimer <= 0) {
                game.returnToMenu();
            }
            return;
        }
        if (titleCard > 0) {
            titleCard = Math.max(0, titleCard - dt);
            if (titleCard == 0 && cassetteActive) {
                sound.tape(true);
            }
            return;
        }
        if (cassetteActive) {
            updateCassette(dt);
            return;
        }
        // En mode test (F2), les effets suivent le temps de jeu accéléré
        double gameDt = dt * timeScale;
        staticTimer = Math.max(0, staticTimer - dt);
        subtitleTimer = Math.max(0, subtitleTimer - dt);
        noseTimer = Math.max(0, noseTimer - dt);
        boolean dark = game.getPower().isPowerOut();
        if (!dark && Math.random() < 0.008) {
            flicker = Draw.random(0.25, 0.6);
        } else {
            flicker = Math.max(0, flicker - dt * 4);
        }
        double energy = game.getPower().getEnergy();
        double flickerChance = energy < 30 ? (30 - energy) / 30 * 0.35 : 0.0;
        leftLightFlicker = Math.random() < flickerChance;
        rightLightFlicker = Math.random() < flickerChance;

        updateHeartbeat(dt);
        updateHallucinations(gameDt, dark);
        updateStorm(gameDt);
        updateGolden(dt);
    }

    private void updateCassette(double dt) {
        cassetteTime += dt;
        int total = cassetteLength();
        int visible = (int) Math.min(total, cassetteTime * TYPE_SPEED);
        if (visible > typedChars) {
            String text = String.join(" ", cassetteLines);
            for (int i = typedChars; i < visible && i < text.length(); i++) {
                if (text.charAt(i) != ' ' && i % 2 == 0) {
                    sound.typewriter();
                    break;
                }
            }
            typedChars = visible;
        }
        if (typedChars >= total && cassetteTime > total / TYPE_SPEED + CASSETTE_LINGER) {
            endCassette();
        }
    }

    private void endCassette() {
        cassetteActive = false;
        sound.tape(false);
    }

    private int cassetteLength() {
        return String.join(" ", cassetteLines).length();
    }

    private void updateHeartbeat(double dt) {
        if (game.getState() == GameState.PLAYING && game.isThreatNear()) {
            beatTimer -= dt;
            if (beatTimer <= 0) {
                sound.heartbeat();
                pulse = 1.0;
                beatTimer = game.getHenry().isPlayingMelody() || game.getPepin().isOut() ? 0.55 : 0.85;
            }
        } else {
            beatTimer = 0;
        }
        pulse = Math.max(0, pulse - dt * 2.2);
    }

    private void updateHallucinations(double dt, boolean dark) {
        hallucinationTimer = Math.max(0, hallucinationTimer - dt);
        if (hallucinationTimer == 0) {
            hallucination = Hallucination.NONE;
        }
        if (!game.hasHallucinations() || dark) {
            return;
        }
        nextHallucination -= dt;
        if (nextHallucination > 0) {
            return;
        }
        nextHallucination = Draw.random(18, 40);
        boolean tablet = game.getOffice().getCameras().isOpen();
        double roll = Math.random();
        if (roll < 0.3) {
            if (Math.random() < 0.5) {
                sound.whisper();
                showSubtitle("* Quelqu'un chuchote, juste derrière vous *");
            } else {
                sound.laugh();
                showSubtitle("* Un rire d'enfant... dans la régie ? *");
            }
        } else if (tablet) {
            hallucination = Hallucination.CAMERA_TEXT;
            hallucinationTimer = 0.7;
        } else {
            hallucination = roll < 0.65 ? Hallucination.POSTER : Hallucination.CLIPPING;
            hallucinationTimer = hallucination == Hallucination.POSTER ? 0.6 : 1.2;
        }
    }

    private void updateStorm(double dt) {
        lightning = Math.max(0, lightning - dt);
        if (thunderDelay > 0) {
            thunderDelay -= dt;
            if (thunderDelay <= 0) {
                sound.thunder();
            }
        }
        nextLightning -= dt;
        if (nextLightning <= 0) {
            nextLightning = Draw.random(14, 35);
            lightning = LIGHTNING_DURATION;
            thunderDelay = Draw.random(0.3, 1.4);
            silhouette = !game.getBobo().isAtDoor() && Math.random() < 0.35;
        }
    }

    /** Intensité de l'éclair : deux flashs rapides. */
    private double flash() {
        if (lightning <= 0) {
            return 0;
        }
        double t = LIGHTNING_DURATION - lightning;
        if (t < 0.08) {
            return 0.75;
        }
        if (t > 0.18 && t < 0.3) {
            return 0.45;
        }
        return 0.08;
    }

    private void updateGolden(double dt) {
        CameraSystem cameras = game.getOffice().getCameras();
        if (goldenRoom == null) {
            return;
        }
        if (!cameras.isOpen() || cameras.getSelected() != goldenRoom) {
            goldenRoom = null;
            goldenStare = 0;
            return;
        }
        goldenStare += dt;
        if (goldenStare >= GOLDEN_STARE) {
            goldenRoom = null;
            goldenStare = 0;
            crashTimer = CRASH_DURATION;
            sound.crash();
        }
    }

    // ------------------------------------------------------------------ dessin

    @Override
    public void render(GraphicsContext gc) {
        if (crashTimer > 0) {
            drawCrash(gc);
            return;
        }
        if (game.getOffice().getCameras().isOpen()) {
            drawCameraView(gc);
        } else {
            drawOffice(gc);
        }
        drawPulse(gc);
        drawTabletBar(gc);
        drawHud(gc);
        if (titleCard > 0) {
            drawTitleCard(gc);
        } else if (cassetteActive) {
            drawCassette(gc);
        }
        if (!isFrozen()) {
            drawPauseButton(gc);
        }
        if (game.getState() == GameState.PAUSED) {
            drawPauseOverlay(gc);
        }
    }

    // ------------------------------------------------------------------ entrées

    @Override
    public void onKey(KeyEvent event) {
        KeyCode code = event.getCode();
        if (crashTimer > 0) {
            return;
        }
        if (titleCard > 0 || cassetteActive) {
            if (code == KeyCode.ENTER || code == KeyCode.SPACE || code == KeyCode.ESCAPE) {
                skipIntro();
            }
            return;
        }
        if (game.getState() == GameState.PAUSED) {
            if (code == KeyCode.P || code == KeyCode.ESCAPE || code == KeyCode.ENTER) {
                game.resume();
            }
            return;
        }
        if (code == KeyCode.P || code == KeyCode.ESCAPE) {
            game.releaseControls();
            game.pause();
            return;
        }
        int camera = cameraNumber(event);
        switch (code) {
            case Q -> game.toggleLeftDoor();
            case E -> game.toggleRightDoor();
            case A -> game.setLeftLight(true);
            case D -> game.setRightLight(true);
            case R -> game.setWinding(true);
            case SPACE -> game.toggleTablet();
            case LEFT -> game.cycleCamera(-1);
            case RIGHT -> game.cycleCamera(1);
            case F2 -> timeScale = timeScale > 1 ? 1.0 : FAST_TIME_SCALE;
            case F3 -> summonGoldenBobo();
            default -> {
                if (camera > 0) {
                    game.selectCamera(camera);
                }
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case A -> game.setLeftLight(false);
            case D -> game.setRightLight(false);
            case R -> game.setWinding(false);
            default -> {
                // Les autres touches ne sont pas maintenues.
            }
        }
    }

    @Override
    public void onMousePressed(double x, double y) {
        if (isFrozen() || game.getState() != GameState.PLAYING) {
            return;
        }
        boolean open = game.getOffice().getCameras().isOpen();
        if (!open && Draw.inside(x, y, BTN_X, LIGHT_Y, BTN_W, LIGHT_H)) {
            game.setLeftLight(true);
        } else if (!open && Draw.inside(x, y, RIGHT_BTN_X, LIGHT_Y, RIGHT_BTN_W, LIGHT_H)) {
            game.setRightLight(true);
        } else if (open && Draw.inside(x, y, ORGAN_X, ORGAN_Y, ORGAN_W, ORGAN_H)) {
            game.setWinding(true);
        }
    }

    @Override
    public void onMouseReleased(double x, double y) {
        game.releaseControls();
    }

    @Override
    public void onClick(double x, double y) {
        if (crashTimer > 0) {
            return;
        }
        if (titleCard > 0 || cassetteActive) {
            skipIntro();
            return;
        }
        if (game.getState() == GameState.PAUSED) {
            if (Draw.inside(x, y, RESUME_X, RESUME_Y, PAUSE_ACTION_W, PAUSE_ACTION_H)) {
                game.resume();
            } else if (Draw.inside(x, y, RESUME_X, MENU_Y, PAUSE_ACTION_W, PAUSE_ACTION_H)) {
                timeScale = 1.0;
                game.returnToMenu();
            }
            return;
        }
        if (Draw.inside(x, y, PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H)) {
            game.releaseControls();
            game.pause();
            return;
        }
        boolean open = game.getOffice().getCameras().isOpen();
        if (Draw.inside(x, y, BAR_X, BAR_Y, BAR_W, BAR_H)) {
            game.toggleTablet();
        } else if (!open && Draw.inside(x, y, BTN_X, BTN_Y, BTN_W, BTN_H)) {
            game.toggleLeftDoor();
        } else if (!open && Draw.inside(x, y, RIGHT_BTN_X, RIGHT_BTN_Y, RIGHT_BTN_W, RIGHT_BTN_H)) {
            game.toggleRightDoor();
        } else if (!open && Math.hypot(x - NOSE_X, y - NOSE_Y) < NOSE_R && !game.getPower().isPowerOut()) {
            sound.honk();
            noseTimer = 0.4;
        } else if (open) {
            cameraAt(x, y).ifPresent(room -> game.selectCamera(room.getCameraNumber()));
        }
    }

    private void skipIntro() {
        if (titleCard > 0) {
            titleCard = 0;
            if (cassetteActive) {
                sound.tape(true);
            }
        } else if (cassetteActive) {
            if (typedChars < cassetteLength()) {
                typedChars = cassetteLength();
                cassetteTime = cassetteLength() / TYPE_SPEED;
            } else {
                endCassette();
            }
        }
    }

    /** F3 : fait apparaître le Bobo doré sur la caméra affichée (pour la démonstration). */
    private void summonGoldenBobo() {
        CameraSystem cameras = game.getOffice().getCameras();
        if (cameras.isOpen()) {
            goldenRoom = cameras.getSelected();
            goldenStare = 0;
        }
    }

    // ------------------------------------------------------------------ événements

    private void onCameraShown(Room camera) {
        staticTimer = SWITCH_STATIC;
        if (goldenRoom == null && Math.random() < GOLDEN_CHANCE) {
            goldenRoom = camera;
            goldenStare = 0;
        }
    }

    private void onAnimatronicMoved(GameEvent.AnimatronicMoved event) {
        CameraSystem cameras = game.getOffice().getCameras();
        if (cameras.isWatching(event.from()) || cameras.isWatching(event.to())) {
            staticTimer = MOVE_STATIC;
        }
        if (Mimi.NAME.equals(event.name())) {
            showSubtitle("* Des craquements d'os résonnent dans le couloir droit *");
            return;
        }
        if (ZigEtZag.NAME.equals(event.name())) {
            showSubtitle("* Des rires d'enfants résonnent dans le chapiteau *");
            return;
        }
        if (event.to() == Room.REGIE || Pepin.NAME.equals(event.name())) {
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
            leftDark = Draw.render(DOOR_W, DOOR_H, g -> paintCorridor(g, Room.COULOIR_GAUCHE, false));
            leftLit = Draw.render(DOOR_W, DOOR_H, g -> paintCorridor(g, Room.COULOIR_GAUCHE, true));
            rightDark = Draw.render(RIGHT_DOOR_W, RIGHT_DOOR_H, g -> paintCorridor(g, Room.COULOIR_DROIT, false));
            rightLit = Draw.render(RIGHT_DOOR_W, RIGHT_DOOR_H, g -> paintCorridor(g, Room.COULOIR_DROIT, true));
        }
        gc.drawImage(officeBackground, 0, 0);
        drawHallucinationOnWall(gc);
        if (noseTimer > 0) {
            gc.setFill(new RadialGradient(0, 0, NOSE_X, NOSE_Y, 26, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 40, 50, 0.95)), new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(NOSE_X - 26, NOSE_Y - 26, 52, 52);
            Draw.shadowText(gc, "POUET !", NOSE_X + 34, NOSE_Y + 30, assets.title(22), Draw.CREAM, TextAlignment.LEFT);
        }
        drawLeftDoor(gc);
        drawRightDoor(gc);
        drawDoorButton(gc, BTN_X, BTN_Y, game.getOffice().getLeftDoor().isClosed(), false, "[Q]");
        drawDoorButton(gc, RIGHT_BTN_X, RIGHT_BTN_Y, game.getOffice().getRightDoor().isClosed(),
                game.getOffice().getRightDoor().isBlocked(), "[E]");
        drawLightButton(gc, BTN_X, game.getOffice().getLeftLight().isOn(), "[A]");
        drawLightButton(gc, RIGHT_BTN_X, game.getOffice().getRightLight().isOn(), "[D]");
        Draw.grain(gc, 575, 444, 180, 100, 90, 0.35);

        if (game.getPower().isPowerOut()) {
            drawBlackout(gc);
        }
        double flash = flash();
        if (flash > 0) {
            gc.setFill(Color.rgb(220, 230, 255, flash * 0.5));
            gc.fillRect(0, 0, W, H);
        }
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

    /** Le couloir vu par l'encadrement, dans le noir ou sous la lampe. */
    private void paintCorridor(GraphicsContext gc, Room corridorRoom, boolean lit) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, DOOR_W, DOOR_H);
        Image corridor = assets.camera(corridorRoom);
        if (corridor != null) {
            double sw = corridor.getWidth() * 0.28;
            double sh = sw * DOOR_H / DOOR_W;
            gc.drawImage(corridor, (corridor.getWidth() - sw) / 2, (corridor.getHeight() - sh) / 2, sw, sh,
                    0, 0, DOOR_W, DOOR_H);
        }
        if (lit) {
            gc.setFill(Color.rgb(0, 0, 0, 0.12));
            gc.fillRect(0, 0, DOOR_W, DOOR_H);
            gc.setFill(new RadialGradient(0, 0, DOOR_W / 2, 40, DOOR_H * 0.9, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 220, 150, 0.30)), new Stop(1, Color.rgb(0, 0, 0, 0.55))));
            gc.fillRect(0, 0, DOOR_W, DOOR_H);
        } else {
            gc.setFill(Color.rgb(0, 0, 0, 0.9));
            gc.fillRect(0, 0, DOOR_W, DOOR_H);
        }
    }

    private void drawLeftDoor(GraphicsContext gc) {
        boolean closed = game.getOffice().getLeftDoor().isClosed();
        boolean lit = (game.getOffice().getLeftLight().isOn() && !leftLightFlicker) || flash() > 0.3;
        gc.drawImage(lit ? leftLit : leftDark, DOOR_X, DOOR_Y);
        if (closed) {
            drawShutter(gc, DOOR_X, DOOR_Y, true);
        } else if (game.getBobo().isAtDoor() && lit) {
            double breathe = Math.sin(time * 2.2) * 4;
            Draw.spriteAt(gc, assets.sprite("bobo", "door", 470), DOOR_X + DOOR_W / 2,
                    DOOR_Y + DOOR_H + 20 + breathe);
        } else if (silhouette && flash() > 0.3) {
            Draw.spriteAt(gc, assets.silhouette("bobo", "door", 470), DOOR_X + DOOR_W / 2, DOOR_Y + DOOR_H + 20);
        }
        drawFrame(gc, DOOR_X, DOOR_Y);
    }

    private void drawRightDoor(GraphicsContext gc) {
        boolean closed = game.getOffice().getRightDoor().isClosed();
        boolean lit = (game.getOffice().getRightLight().isOn() && !rightLightFlicker) || flash() > 0.3;
        gc.drawImage(lit ? rightLit : rightDark, RIGHT_DOOR_X, RIGHT_DOOR_Y);
        if (closed) {
            drawShutter(gc, RIGHT_DOOR_X, RIGHT_DOOR_Y, false);
        } else if (game.getMimi().getRoom() == Room.COULOIR_DROIT && lit) {
            double breathe = Math.sin(time * 2.2) * 4;
            Draw.spriteAt(gc, assets.sprite("mimi", "idle_2", 420),
                    RIGHT_DOOR_X + RIGHT_DOOR_W / 2, RIGHT_DOOR_Y + RIGHT_DOOR_H + 10 + breathe);
        }
        if (game.getMimi().isAtEmbrasure()) {
            Draw.text(gc, "PORTE BLOQUÉE", RIGHT_DOOR_X + RIGHT_DOOR_W / 2,
                    RIGHT_DOOR_Y + 30, assets.ui(18), Color.web("#ff5260"), TextAlignment.CENTER);
        }
        drawFrame(gc, RIGHT_DOOR_X, RIGHT_DOOR_Y);
    }

    private void drawFrame(GraphicsContext gc, double x, double y) {
        gc.setStroke(Color.web("#2e2326"));
        gc.setLineWidth(18);
        gc.strokeRect(x, y, DOOR_W, DOOR_H);
        gc.setStroke(Color.web("#4a3a2c"));
        gc.setLineWidth(3);
        gc.strokeRect(x - 9, y - 9, DOOR_W + 18, DOOR_H + 18);
    }

    private void drawShutter(GraphicsContext gc, double x, double y, boolean hazard) {
        gc.setFill(new LinearGradient(x, 0, x + DOOR_W, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2c2a2b")), new Stop(0.5, Color.web("#4a4648")), new Stop(1, Color.web("#2c2a2b"))));
        gc.fillRect(x, y, DOOR_W, DOOR_H);
        gc.setStroke(Color.rgb(0, 0, 0, 0.45));
        gc.setLineWidth(4);
        for (double ly = y + 24; ly < y + DOOR_H - 40; ly += 30) {
            gc.strokeLine(x, ly, x + DOOR_W, ly);
        }
        if (!hazard) {
            return;
        }
        // Bande de danger en bas du rideau
        gc.save();
        gc.beginPath();
        gc.rect(x, y + DOOR_H - 40, DOOR_W, 40);
        gc.closePath();
        gc.clip();
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(x, y + DOOR_H - 40, DOOR_W, 40);
        gc.setFill(Color.web("#b8901c"));
        for (double sx = x - 40; sx < x + DOOR_W + 40; sx += 40) {
            gc.fillPolygon(new double[]{sx, sx + 20, sx + 60, sx + 40},
                    new double[]{y + DOOR_H, y + DOOR_H, y + DOOR_H - 40, y + DOOR_H - 40}, 4);
        }
        gc.restore();
    }

    private void drawDoorButton(GraphicsContext gc, double bx, double by, boolean closed, boolean blocked, String key) {
        gc.setFill(Color.web("#2a2426"));
        gc.fillRect(bx, by, BTN_W, BTN_H);
        gc.setStroke(blocked ? Color.web("#e0303f") : Color.web("#4a3f3a"));
        gc.setLineWidth(2);
        gc.strokeRect(bx + 4, by + 4, BTN_W - 8, BTN_H - 8);
        double cx = bx + BTN_W / 2;
        if (closed) {
            gc.setFill(new RadialGradient(0, 0, cx, by + 46, 60, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 60, 70, 0.55)), new Stop(1, Color.TRANSPARENT)));
            gc.fillRect(bx - 30, by - 20, BTN_W + 60, 140);
        }
        gc.setFill(closed ? Color.web("#e0303f") : Color.web("#4a1a1f"));
        gc.fillOval(cx - 28, by + 18, 56, 56);
        gc.setStroke(Color.web("#120808"));
        gc.setLineWidth(3);
        gc.strokeOval(cx - 28, by + 18, 56, 56);
        Draw.text(gc, blocked ? "BLOQUÉE" : "PORTE", cx, by + 96, assets.ui(15),
                blocked ? Color.web("#ff5260") : Draw.TEXT, TextAlignment.CENTER);
        Draw.text(gc, key, cx, by + 118, assets.ui(13), Draw.TEXT_DIM, TextAlignment.CENTER);
    }

    private void drawLightButton(GraphicsContext gc, double bx, boolean on, String key) {
        gc.setFill(Color.web("#2a2426"));
        gc.fillRect(bx, LIGHT_Y, BTN_W, LIGHT_H);
        gc.setStroke(on ? Draw.AMBER : Color.web("#4a3f3a"));
        gc.setLineWidth(2);
        gc.strokeRect(bx + 4, LIGHT_Y + 4, BTN_W - 8, LIGHT_H - 8);
        double cx = bx + BTN_W / 2;
        if (on) {
            gc.setFill(new RadialGradient(0, 0, cx, LIGHT_Y + 22, 40, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 210, 120, 0.6)), new Stop(1, Color.TRANSPARENT)));
            gc.fillRect(bx - 20, LIGHT_Y - 18, BTN_W + 40, 80);
        }
        gc.setFill(on ? Color.web("#ffd27a") : Color.web("#3a3428"));
        gc.fillOval(cx - 11, LIGHT_Y + 11, 22, 22);
        gc.setStroke(Color.web("#120808"));
        gc.setLineWidth(2);
        gc.strokeOval(cx - 11, LIGHT_Y + 11, 22, 22);
        Draw.text(gc, "LUMIÈRE " + key, cx, LIGHT_Y + 50, assets.ui(12), on ? Draw.AMBER : Draw.TEXT_DIM,
                TextAlignment.CENTER);
    }

    private void drawPoster(GraphicsContext gc) {
        gc.save();
        gc.translate(W / 2, 260);
        gc.rotate(2.5);
        gc.setFill(Draw.RED_DARK);
        gc.fillRect(-125, -160, 250, 330);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(3);
        gc.strokeRect(-113, -148, 226, 306);
        Draw.text(gc, "CIRQUE", 0, -95, assets.title(40), Draw.GOLD, TextAlignment.CENTER);
        Draw.text(gc, "MALVAUX", 0, -50, assets.title(40), Draw.GOLD, TextAlignment.CENTER);
        Draw.sprite(gc, assets.sprite("bobo", "idle_1"), 0, 110, 150);
        // Nez rouge de clown, peint sur l'affiche
        gc.setFill(Color.web("#c8202c"));
        gc.fillOval(-6, -20, 12, 12);
        gc.setFill(Color.rgb(255, 255, 255, 0.5));
        gc.fillOval(-3, -18, 4, 4);
        Draw.text(gc, "DERNIÈRE REPRÉSENTATION", 0, 125, assets.ui(13), Draw.CREAM, TextAlignment.CENTER);
        Draw.text(gc, "31 OCTOBRE 1994", 0, 145, assets.ui(15), Draw.CREAM, TextAlignment.CENTER);
        gc.restore();
    }

    private void drawClipping(GraphicsContext gc) {
        gc.save();
        gc.translate(CLIP_X, CLIP_Y);
        gc.rotate(-5);
        gc.setFill(Color.web("#b9b09a"));
        gc.fillRect(-62, -48, 124, 96);
        Draw.text(gc, "INCENDIE", 0, -30, assets.ui(13), Color.web("#1d1a16"), TextAlignment.CENTER);
        Draw.text(gc, "AU CIRQUE", 0, -14, assets.ui(13), Color.web("#1d1a16"), TextAlignment.CENTER);
        Draw.text(gc, "Les clowns restent", 0, 8, assets.ui(10), Color.web("#3a342c"), TextAlignment.CENTER);
        Draw.text(gc, "introuvables.", 0, 21, assets.ui(10), Color.web("#3a342c"), TextAlignment.CENTER);
        Draw.text(gc, "1er nov. 1994", 0, 38, assets.ui(9), Color.web("#5a5246"), TextAlignment.CENTER);
        gc.restore();
    }

    /** Hallucinations sur les murs de la régie : l'affiche ou la coupure de journal changent. */
    private void drawHallucinationOnWall(GraphicsContext gc) {
        if (hallucination == Hallucination.POSTER) {
            // Bobo s'est tourné vers vous : son visage remplit l'affiche
            gc.save();
            gc.translate(W / 2, 260);
            gc.rotate(2.5);
            gc.beginPath();
            gc.rect(-113, -148, 226, 306);
            gc.closePath();
            gc.clip();
            gc.setFill(Color.web("#2a0508"));
            gc.fillRect(-113, -148, 226, 306);
            Draw.sprite(gc, assets.sprite("bobo", "scare_1"), Draw.random(-3, 3), 300, 470);
            gc.setFill(Color.rgb(160, 0, 10, 0.25));
            gc.fillRect(-113, -148, 226, 306);
            gc.restore();
        } else if (hallucination == Hallucination.CLIPPING) {
            gc.save();
            gc.translate(CLIP_X, CLIP_Y);
            gc.rotate(-5);
            gc.setFill(Color.web("#b9b09a"));
            gc.fillRect(-62, -48, 124, 96);
            Draw.text(gc, "VOUS ÊTES", 0, -14, assets.title(16), Color.web("#6a0d14"), TextAlignment.CENTER);
            Draw.text(gc, "LE PROCHAIN", 0, 12, assets.title(15), Color.web("#6a0d14"), TextAlignment.CENTER);
            gc.restore();
        }
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

    /** Coupure de courant : noir presque total, puis Henry et sa mélodie dans la porte gauche. */
    private void drawBlackout(GraphicsContext gc) {
        Draw.shade(gc, 0.9);
        if (!game.getHenry().isPlayingMelody()) {
            return;
        }
        // Son visage apparaît et disparaît au rythme de la boîte à musique
        double beat = (time * 2.0) % 1.0;
        double cx = DOOR_X + DOOR_W / 2;
        if (beat < 0.45 || Math.random() < 0.05) {
            gc.setGlobalAlpha(0.55);
            Draw.spriteAt(gc, assets.sprite("henry", "idle_1", 470), cx, DOOR_Y + DOOR_H + 20);
            gc.setGlobalAlpha(1.0);
        }
        for (double ex : new double[]{cx - 13, cx + 11}) {
            gc.setFill(new RadialGradient(0, 0, ex, 196, 12, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 245, 210, 0.95)), new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(ex - 12, 184, 24, 24);
        }
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
            drawPepin(gc);
        }
        drawCharacterOnCamera(gc, room, game.getMimi().getRoom(), "mimi", 420);
        drawCharacterOnCamera(gc, room, game.getZigEtZag().getRoom(), ZigEtZag.SPRITE_ID, 390);
        if (goldenRoom == room) {
            Draw.spriteAt(gc, assets.golden("bobo", "idle_3", 380), W / 2 - 60, 690);
        }

        Draw.overlay(gc, 0.3, 0.25, 0.85);
        boolean disabled = cameras.isDisabled(room);
        boolean noisy = staticTimer > 0 || disabled;
        Draw.grain(gc, noisy ? 2200 : 220, noisy ? 0.6 : 0.12);
        if (disabled) {
            gc.setFill(Color.rgb(190, 190, 190, 0.78));
            gc.fillRect(0, 0, W, H);
            if (cameras.getDisabledTimeRemaining(room) > ZigEtZag.CAMERA_DISABLE_DURATION - 1.5) {
                gc.setGlobalAlpha(0.9);
                Draw.spriteAt(gc, assets.sprite(ZigEtZag.SPRITE_ID, "scare_1", 490), W / 2, 620);
                gc.setGlobalAlpha(1.0);
            }
            Draw.text(gc, "SIGNAL PERDU", W / 2, H / 2, assets.title(48), Color.WHITE, TextAlignment.CENTER);
        } else if (noisy) {
            gc.setFill(Color.rgb(160, 160, 160, 0.25 * Math.min(1.0, staticTimer / SWITCH_STATIC)));
            gc.fillRect(0, 0, W, H);
        }
        if (hallucination == Hallucination.CAMERA_TEXT && ((int) (time * 12)) % 3 != 0) {
            Draw.shadowText(gc, "DERRIÈRE TOI", W / 2 + Draw.random(-3, 3), H / 2 + 10, assets.title(84),
                    Color.web("#d0202e"), TextAlignment.CENTER);
        }
        double flash = flash();
        if (flash > 0) {
            gc.setFill(Color.rgb(220, 230, 255, flash * 0.18));
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
        if (room == Room.FOSSE_ORCHESTRE && !disabled) {
            drawOrganPanel(gc);
        }
        drawCameraMap(gc, cameras);
    }

    private void drawPepin(GraphicsContext gc) {
        if (game.getPepin().isOut()) {
            double shake = Draw.random(-6, 6);
            Draw.spriteAt(gc, assets.sprite(Pepin.SPRITE_ID, "grin", 520), PEPIN_ON_CAMERA[0] + shake, 700);
            return;
        }
        String frame = new String[]{"idle_1", "idle_2", "idle_3"}[(int) (time * 0.8) % 3];
        Draw.spriteAt(gc, assets.sprite(Pepin.SPRITE_ID, frame, PEPIN_ON_CAMERA[2]),
                PEPIN_ON_CAMERA[0], PEPIN_ON_CAMERA[1]);
    }

    /** Jauge et manivelle de l'orgue de barbarie, sur la caméra 8. */
    private void drawOrganPanel(GraphicsContext gc) {
        BarrelOrgan organ = game.getOffice().getOrgan();
        boolean winding = organ.isWinding();
        gc.setFill(Color.rgb(10, 8, 8, 0.82));
        gc.fillRoundRect(ORGAN_X, ORGAN_Y, ORGAN_W, ORGAN_H, 10, 10);
        gc.setStroke(winding ? Draw.AMBER : Draw.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(ORGAN_X, ORGAN_Y, ORGAN_W, ORGAN_H, 10, 10);
        Draw.text(gc, "ORGUE DE BARBARIE", ORGAN_X + 20, ORGAN_Y + 26, assets.ui(17), Draw.CREAM, TextAlignment.LEFT);

        double charge = organ.getCharge() / BarrelOrgan.MAX_CHARGE;
        Color level = charge > 0.5 ? Color.web("#7fbf6a") : charge > 0.25 ? Draw.AMBER : Color.web("#e0303f");
        double barX = ORGAN_X + 20;
        double barY = ORGAN_Y + 46;
        double barW = ORGAN_W - 110;
        gc.setFill(Color.rgb(255, 255, 255, 0.08));
        gc.fillRect(barX, barY, barW, 22);
        gc.setFill(level);
        gc.fillRect(barX, barY, barW * charge, 22);
        Draw.text(gc, (int) Math.ceil(organ.getCharge()) + " %", barX + barW + 12, barY + 11, assets.ui(16),
                level, TextAlignment.LEFT);

        // Manivelle qui tourne quand on la tient
        double cx = ORGAN_X + ORGAN_W - 40;
        double cy = ORGAN_Y + 100;
        double angle = winding ? time * 8 : 0.6;
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(4);
        gc.strokeOval(cx - 4, cy - 4, 8, 8);
        gc.strokeLine(cx, cy, cx + Math.cos(angle) * 22, cy + Math.sin(angle) * 22);
        gc.setFill(Draw.CREAM);
        gc.fillOval(cx + Math.cos(angle) * 22 - 5, cy + Math.sin(angle) * 22 - 5, 10, 10);

        String hint;
        Color hintColor = Draw.TEXT_DIM;
        if (game.getPepin().isOut()) {
            hint = "PÉPIN EST SORTI ! (" + (int) Math.ceil(game.getPepin().getEscapeTimeRemaining()) + " s)";
            hintColor = Color.web("#ff5260");
        } else if (winding) {
            hint = "Vous remontez l'orgue...";
            hintColor = Draw.AMBER;
        } else {
            hint = "[R] ou clic maintenu : remonter";
        }
        Draw.text(gc, hint, ORGAN_X + 20, ORGAN_Y + 100, assets.ui(15), hintColor, TextAlignment.LEFT);
    }

    private void drawCharacterOnCamera(GraphicsContext gc, Room camera, Room characterRoom,
                                       String spriteId, double height) {
        if (characterRoom != camera) {
            return;
        }
        double[] position = switch (camera) {
            case LOGES, MENAGERIE -> new double[]{690, 650};
            case COULOIR_DROIT -> new double[]{620, 610};
            default -> new double[]{W / 2, 650};
        };
        String frame = new String[]{"idle_1", "idle_2", "idle_1", "idle_3"}[(int) (time * 1.3) % 4];
        Draw.spriteAt(gc, assets.sprite(spriteId, frame, height), position[0], position[1]);
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

    /** Halo rouge qui bat avec le cœur quand un clown est tout proche. */
    private void drawPulse(GraphicsContext gc) {
        if (pulse <= 0) {
            return;
        }
        gc.setFill(new RadialGradient(0, 0, W / 2, H / 2, W * 0.7, false, CycleMethod.NO_CYCLE,
                new Stop(0.55, Color.TRANSPARENT), new Stop(1, Color.rgb(150, 0, 10, 0.45 * pulse))));
        gc.fillRect(0, 0, W, H);
    }

    private void drawTabletBar(GraphicsContext gc) {
        boolean open = game.getOffice().getCameras().isOpen();
        boolean dark = game.getPower().isPowerOut();
        gc.setFill(Color.rgb(20, 18, 18, 0.88));
        gc.fillRect(BAR_X, BAR_Y, BAR_W, BAR_H);
        gc.setStroke(open ? Draw.AMBER : Draw.TEXT_DIM);
        gc.setLineWidth(1.5);
        gc.strokeRect(BAR_X, BAR_Y, BAR_W, BAR_H);
        String label = dark ? "PLUS DE COURANT" : open ? "BAISSER LA TABLETTE  [Espace]" : "CAMÉRAS  [Espace]";
        Draw.text(gc, label, BAR_X + BAR_W / 2, BAR_Y + BAR_H / 2, assets.ui(16),
                dark ? Color.web("#e0303f") : open ? Draw.AMBER : Draw.TEXT, TextAlignment.CENTER);
    }

    private void drawHud(GraphicsContext gc) {
        // Heure et nuit
        Draw.shadowText(gc, game.getClock().getFormattedTime(), W - 40, 52, assets.title(48), Draw.CREAM,
                TextAlignment.RIGHT);
        String nightLabel = game.isCustomNight() ? "NUIT PERSONNALISÉE" : "NUIT " + game.getCurrentNight();
        Draw.shadowText(gc, nightLabel, W - 42, 96, assets.ui(20), Draw.TEXT, TextAlignment.RIGHT);

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
        int usage = game.getPower().isPowerOut() ? 0 : game.getPower().computeUsage();
        for (int i = 0; i < 6; i++) {
            gc.setFill(i < usage ? (i == 0 ? Color.web("#7fbf6a") : i < 3 ? Draw.AMBER : Color.web("#e0303f"))
                    : Color.rgb(255, 255, 255, 0.08));
            gc.fillRect(318 + i * 14, 676, 10, 14);
        }

        if (timeScale > 1) {
            Draw.text(gc, "TEMPS x" + (int) timeScale + "  (F2)", 40, 132, assets.ui(16), Draw.AMBER, TextAlignment.LEFT);
        }

        // Alerte de l'orgue, visible depuis la régie comme depuis les autres caméras
        BarrelOrgan organ = game.getOffice().getOrgan();
        boolean onOrganCamera = game.getOffice().getCameras().isWatching(Room.FOSSE_ORCHESTRE);
        if (!game.getPower().isPowerOut() && !onOrganCamera && organ.getCharge() < ORGAN_WARNING
                && ((int) (time * 3)) % 2 == 0) {
            String text = game.getPepin().isOut() ? "! PÉPIN EST SORTI - CAM 8 !"
                    : "! ORGUE " + (int) Math.ceil(organ.getCharge()) + " % - CAM 8";
            Draw.shadowText(gc, text, W - 40, 134, assets.ui(18),
                    game.getPepin().isOut() ? Color.web("#ff5260") : Draw.AMBER, TextAlignment.RIGHT);
        }

        if (subtitleTimer > 0 && subtitle != null) {
            gc.setGlobalAlpha(Math.min(1.0, subtitleTimer));
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.fillRect(W / 2 - 330, 606, 660, 36);
            Draw.text(gc, subtitle, W / 2, 624, assets.ui(19), Draw.CREAM, TextAlignment.CENTER);
            gc.setGlobalAlpha(1.0);
        }
        if (game.getMimi().isAtEmbrasure()) {
            int seconds = (int) Math.ceil(game.getMimi().getEmbrasureTimeRemaining());
            Draw.text(gc, "MIMI BLOQUE LA PORTE - REGARDEZ CAM 7 (" + seconds + " s)",
                    W / 2, 570, assets.ui(18), Color.web("#ff5260"), TextAlignment.CENTER);
        }
    }

    private void drawTitleCard(GraphicsContext gc) {
        double alpha = cassetteActive ? 1.0 : Math.min(1.0, titleCard / 1.0);
        gc.setGlobalAlpha(alpha);
        Draw.clear(gc, Color.BLACK);
        int night = game.getCurrentNight();
        Draw.text(gc, "00:00", W / 2, H / 2 - 110, assets.ui(26), Draw.TEXT_DIM, TextAlignment.CENTER);
        if (game.isCustomNight()) {
            Draw.text(gc, "NUIT PERSONNALISÉE", W / 2, H / 2 - 30, assets.title(64), Draw.RED, TextAlignment.CENTER);
            Draw.text(gc, "Vous avez choisi votre troupe. Elle vous a choisi aussi.", W / 2, H / 2 + 60,
                    assets.ui(20), Draw.TEXT, TextAlignment.CENTER);
        } else {
            Draw.text(gc, "NUIT " + night, W / 2, H / 2 - 30, assets.title(88), Draw.RED, TextAlignment.CENTER);
            Draw.text(gc, NIGHT_LINES[Math.min(night, NIGHT_LINES.length) - 1], W / 2, H / 2 + 60,
                    assets.ui(20), Draw.TEXT, TextAlignment.CENTER);
        }
        Draw.text(gc, "[Entrée] Passer", W / 2, H - 50, assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
        gc.setGlobalAlpha(1.0);
    }

    /** La cassette du veilleur : texte tapé à la machine sur fond de bande qui tourne. */
    private void drawCassette(GraphicsContext gc) {
        Draw.clear(gc, Color.web("#070505"));
        // Cassette dessinée
        double cx = W / 2;
        double cy = 150;
        gc.setFill(Color.web("#1c1816"));
        gc.fillRoundRect(cx - 170, cy - 70, 340, 140, 14, 14);
        gc.setFill(Color.web("#d8cfb8"));
        gc.fillRect(cx - 140, cy - 56, 280, 44);
        Draw.text(gc, "VEILLE - NUIT " + game.getCurrentNight() + " - MARCEL", cx, cy - 34, assets.ui(15),
                Color.web("#2a2420"), TextAlignment.CENTER);
        gc.setFill(Color.web("#0d0b0a"));
        gc.fillRoundRect(cx - 110, cy + 2, 220, 48, 24, 24);
        for (double rx : new double[]{cx - 70, cx + 70}) {
            gc.setFill(Color.web("#3a3430"));
            gc.fillOval(rx - 20, cy + 6, 40, 40);
            gc.setStroke(Color.web("#d8cfb8"));
            gc.setLineWidth(3);
            double a = time * 4;
            for (int k = 0; k < 3; k++) {
                double ang = a + k * Math.PI * 2 / 3;
                gc.strokeLine(rx, cy + 26, rx + Math.cos(ang) * 14, cy + 26 + Math.sin(ang) * 14);
            }
        }
        if (((int) (time * 1.5)) % 2 == 0) {
            gc.setFill(Draw.RED);
            gc.fillOval(cx - 262, cy - 6, 12, 12);
        }
        Draw.text(gc, "LECTURE", cx - 244, cy, assets.ui(13), Draw.TEXT_DIM, TextAlignment.LEFT);

        // Texte tapé à la machine
        int remaining = typedChars;
        double y = 300;
        for (String line : cassetteLines) {
            if (remaining <= 0) {
                break;
            }
            String shown = line.substring(0, Math.min(line.length(), remaining));
            Draw.text(gc, shown, 190, y, assets.ui(22), Draw.CREAM, TextAlignment.LEFT);
            remaining -= line.length() + 1;
            y += 36;
        }
        if (((int) (time * 2)) % 2 == 0 && typedChars < cassetteLength()) {
            gc.setFill(Draw.CREAM);
            gc.fillRect(190, y - 36 + 12, 12, 3);
        }
        Draw.text(gc, typedChars < cassetteLength() ? "[Entrée] Tout afficher" : "[Entrée] Commencer la nuit",
                W / 2, H - 50, assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
        Draw.grain(gc, 500, 0.15);
        Draw.overlay(gc, 0, 0.2, 0.9);
    }

    /** Faux plantage du jeu après avoir trop regardé le Bobo doré. */
    private void drawCrash(GraphicsContext gc) {
        Draw.clear(gc, Color.BLACK);
        double elapsed = CRASH_DURATION - crashTimer;
        if (elapsed < 0.35) {
            Draw.spriteAt(gc, assets.golden("bobo", "scare_2", H * 1.4), W / 2, H * 1.25);
            return;
        }
        String[] lines = {
            "Exception in thread \"JavaFX Application Thread\" java.lang.IllegalStateException: IL EST ICI",
            "    at com.epitech.fnah.entity.Bobo.regarde(Bobo.java:1994)",
            "    at com.epitech.fnah.core.GameLoop.tick(GameLoop.java:666)",
            "    at com.epitech.fnah.view.NightScreen.render(NightScreen.java:31)",
            "    at javafx.graphics/com.sun.javafx.tk.Toolkit.lambda$runPulse(Toolkit.java:404)",
            "    ... 13 more",
            "",
            "Caused by: java.lang.NullPointerException: le veilleur est null",
            "",
            "Le programme a cessé de fonctionner.",
        };
        for (int i = 0; i < lines.length; i++) {
            Draw.text(gc, lines[i], 40, 60 + i * 26, javafx.scene.text.Font.font("Monospaced", 16),
                    Color.rgb(200, 200, 200), TextAlignment.LEFT);
        }
    }

    private void drawPauseButton(GraphicsContext gc) {
        gc.setFill(Color.rgb(20, 18, 18, 0.88));
        gc.fillRoundRect(PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H, 8, 8);
        gc.setStroke(Draw.TEXT_DIM);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H, 8, 8);
        Draw.text(gc, "PAUSE  [P]", PAUSE_X + PAUSE_W / 2, PAUSE_Y + PAUSE_H / 2,
                assets.ui(14), Draw.CREAM, TextAlignment.CENTER);
    }

    private void drawPauseOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, W, H);
        gc.setFill(Color.web("#1a1012"));
        gc.fillRoundRect(PAUSE_PANEL_X, PAUSE_PANEL_Y, PAUSE_PANEL_W, PAUSE_PANEL_H, 16, 16);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(PAUSE_PANEL_X, PAUSE_PANEL_Y, PAUSE_PANEL_W, PAUSE_PANEL_H, 16, 16);
        Draw.text(gc, "PAUSE", W / 2, PAUSE_PANEL_Y + 65, assets.title(48), Draw.CREAM, TextAlignment.CENTER);
        drawPauseAction(gc, RESUME_Y, "REPRENDRE  [P / ÉCHAP]");
        drawPauseAction(gc, MENU_Y, "RETOUR AU MENU");
    }

    private void drawPauseAction(GraphicsContext gc, double y, String label) {
        gc.setFill(Color.web("#2a2426"));
        gc.fillRoundRect(RESUME_X, y, PAUSE_ACTION_W, PAUSE_ACTION_H, 8, 8);
        gc.setStroke(Draw.TEXT_DIM);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(RESUME_X, y, PAUSE_ACTION_W, PAUSE_ACTION_H, 8, 8);
        Draw.text(gc, label, W / 2, y + PAUSE_ACTION_H / 2, assets.ui(17), Draw.CREAM, TextAlignment.CENTER);
    }

    /**
     * Coupe un texte en lignes d'au plus {@code width} caractères, sans couper les mots.
     *
     * @param text  texte à couper
     * @param width nombre de caractères maximal par ligne
     * @return les lignes
     */
    static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
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
