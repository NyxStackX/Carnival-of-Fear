package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.core.TerrorDirector;
import com.epitech.fnah.entity.Mimi;
import com.epitech.fnah.entity.Pepin;
import com.epitech.fnah.entity.ZigEtZag;
import com.epitech.fnah.model.BarrelOrgan;
import com.epitech.fnah.model.CameraSystem;
import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.FuseBox;
import com.epitech.fnah.model.NoiseMeter;
import com.epitech.fnah.model.Safe;
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
    // Onglet « Caméras » et pause
    private static final double BAR_X = 440, BAR_Y = 674, BAR_W = 400, BAR_H = 46;
    private static final double PAUSE_X = 1100, PAUSE_Y = 672, PAUSE_W = 156, PAUSE_H = 36;
    private static final double PAUSE_PANEL_X = 390, PAUSE_PANEL_Y = 190, PAUSE_PANEL_W = 500, PAUSE_PANEL_H = 330;
    private static final double RESUME_X = 490, RESUME_Y = 320, PAUSE_ACTION_W = 300, PAUSE_ACTION_H = 52;
    private static final double MENU_Y = 390;
    // Plan des caméras (sous un bandeau de titre)
    private static final double MAP_X = 950, MAP_Y = 360, MAP_W = 300, MAP_H = 292, MAP_HEAD = 26;
    private static final double CAM_W = 64, CAM_H = 28;
    // Manivelle de l'orgue (caméra 8)
    private static final double ORGAN_X = 40, ORGAN_Y = 430, ORGAN_W = 340, ORGAN_H = 140;
    // Disjoncteur (au-dessus de la plaque gauche), coffre (au sol, à droite du bureau), carnet
    private static final double FUSE_X = 382, FUSE_Y = 90, FUSE_W = 90, FUSE_H = 106;
    private static final double SAFE_X = 868, SAFE_Y = 612, SAFE_W = 92, SAFE_H = 70;
    private static final double NOTE_X = 352, NOTE_Y = 618, NOTE_W = 100, NOTE_H = 70;
    private static final double CUT_X = 783, CUT_Y = 100, CUT_W = 126, CUT_H = 100;
    // Bouton « se cacher », au-dessus de la pause
    private static final double HIDE_X = 1100, HIDE_Y = 626, HIDE_W = 156, HIDE_H = 36;
    // Décalage qui centre le plan des caméras dans son cadre
    private static final double MAP_DX = 32, MAP_DY = 6;
    private static final double FADE_IN = 1.8;
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
    private static final int CASSETTE_LINE = 58;
    /** Ce que la bande murmure quand elle se dérègle, une phrase par nuit. */
    private static final String[] TAPE_WHISPERS = {
        "NE TE RETOURNE PAS", "ELLE EST DÉJÀ DANS LE COULOIR", "TU ENTENDS L'ORGUE ?",
        "IL CHANTE POUR TOI", "RESTE AVEC NOUS",
    };
    /** Proportion de la cassette à laquelle la bande se dérègle. */
    private static final double GLITCH_AT = 0.55;
    private static final double GLITCH_DURATION = 2.2;
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
    private Image mapBase;

    private double time;
    private double titleCard;
    private boolean cassetteActive;
    private double cassetteTime;
    private int typedChars;
    private List<String> cassetteLines = List.of();
    private double glitchTimer;
    private boolean glitchDone;
    private double reelAngle;
    private Image cassetteBackground;
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
    // Fenêtres du bureau (coffre, carnet, coupure de journal)
    private final DeskOverlays overlays;
    // Arrivée dans la régie, après la cassette
    private double fadeIn;
    private boolean arrived;
    // Événements du metteur en scène de la peur
    private boolean cupFallen;
    private double lampOff;
    private Door.Side shadowSide;
    private double shadowTimer;
    private Room faceRoom;
    private double faceTimer;
    private Image officeShadow;
    // Souris, pour le survol des boutons
    private double mouseX = -1;
    private double mouseY = -1;

    NightScreen(Game game, Assets assets, SoundManager sound) {
        this.game = game;
        this.assets = assets;
        this.sound = sound;
        this.overlays = new DeskOverlays(game, assets);
        var events = game.getEvents();
        events.subscribe(GameEvent.CameraSelected.class, e -> onCameraShown(e.camera()));
        events.subscribe(GameEvent.TabletToggled.class, e -> {
            if (e.open()) {
                onCameraShown(e.camera());
            }
        });
        events.subscribe(GameEvent.AnimatronicMoved.class, this::onAnimatronicMoved);
        events.subscribe(GameEvent.PowerOut.class, e -> showSubtitle("* Un claquement. Puis plus rien. *"));
        events.subscribe(GameEvent.ScareEvent.class, e -> onScare(e.scare(), e.pan()));
        events.subscribe(GameEvent.FuseChanged.class, e -> showSubtitle(e.tripped() == null
                ? "* Le courant revient dans le circuit *"
                : e.tripped() == FuseBox.Circuit.CAMERAS ? "* Un fusible saute : les caméras sont mortes *"
                : "* Un fusible saute : les lampes des couloirs sont mortes *"));
        events.subscribe(GameEvent.ClueFound.class, e -> showSubtitle("* Un chiffre griffonné sur le mur : "
                + new String[]{"I", "II", "III"}[e.index()] + " = " + e.digit() + " (noté dans le carnet) *"));
        events.subscribe(GameEvent.TabletToggled.class, e -> overlays.close());
        events.subscribe(GameEvent.PlayerHidden.class, e -> overlays.close());
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
            glitchTimer = 0;
            glitchDone = false;
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
            fadeIn = FADE_IN;
            arrived = false;
            cupFallen = false;
            lampOff = 0;
            shadowSide = null;
            faceRoom = null;
            overlays.close();
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
        if (!arrived) {
            // La porte de la régie claque derrière le veilleur
            arrived = true;
            sound.thud();
        }
        fadeIn = Math.max(0, fadeIn - dt);
        overlays.update(dt);
        // En mode test (F2), les effets suivent le temps de jeu accéléré
        double gameDt = dt * timeScale;
        lampOff = Math.max(0, lampOff - gameDt);
        shadowTimer = Math.max(0, shadowTimer - gameDt);
        if (shadowTimer == 0) {
            shadowSide = null;
        }
        faceTimer = Math.max(0, faceTimer - gameDt);
        if (faceTimer == 0) {
            faceRoom = null;
        }
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
        int total = cassetteLength();
        // La bande se dérègle une fois, au milieu : elle s'arrête, quelque chose murmure
        if (glitchTimer > 0) {
            glitchTimer = Math.max(0, glitchTimer - dt);
            reelAngle += dt * (Math.random() < 0.5 ? -9 : 14);
            if (glitchTimer == 0) {
                sound.tape(true);
            }
            return;
        }
        if (!glitchDone && typedChars >= total * GLITCH_AT && typedChars < total) {
            glitchDone = true;
            glitchTimer = GLITCH_DURATION;
            sound.tape(false);
            sound.tapeGlitch();
            pulse = 1.0;
            return;
        }
        cassetteTime += dt;
        reelAngle += dt * (typedChars < total ? 3.4 : 1.2);
        int visible = (int) Math.min(total, cassetteTime * TYPE_SPEED);
        if (visible > typedChars) {
            String text = String.join(" ", cassetteLines);
            for (int i = typedChars; i < visible && i < text.length(); i++) {
                if (text.charAt(i) != ' ' && i % 3 == 0) {
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
        } else if (game.getOffice().isHidden()) {
            drawHidden(gc);
        } else {
            drawOffice(gc);
        }
        drawPulse(gc);
        drawTabletBar(gc);
        drawHud(gc);
        overlays.render(gc);
        if (fadeIn > 0 && !isFrozen()) {
            Draw.shade(gc, fadeIn / FADE_IN);
        }
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
        if (overlays.onKey(event)) {
            return;
        }
        if (code == KeyCode.P || code == KeyCode.ESCAPE) {
            game.releaseControls();
            game.pause();
            return;
        }
        int camera = cameraNumber(event);
        switch (code) {
            case S -> game.toggleHide();
            case F -> game.setFuseReset(true);
            case J -> {
                if (canUseDesk()) {
                    overlays.open(DeskOverlays.Kind.JOURNAL);
                }
            }
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
            case F -> game.setFuseReset(false);
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
        if (!open && !overlays.isOpen() && Draw.inside(x, y, FUSE_X, FUSE_Y, FUSE_W, FUSE_H)) {
            game.setFuseReset(true);
        } else if (!open && Draw.inside(x, y, BTN_X, LIGHT_Y, BTN_W, LIGHT_H)) {
            game.setLeftLight(true);
        } else if (!open && Draw.inside(x, y, RIGHT_BTN_X, LIGHT_Y, RIGHT_BTN_W, LIGHT_H)) {
            game.setRightLight(true);
        } else if (open && Draw.inside(x, y, ORGAN_X, ORGAN_Y, ORGAN_W, ORGAN_H)) {
            game.setWinding(true);
        }
    }

    @Override
    public void onMouseMove(double x, double y) {
        mouseX = x;
        mouseY = y;
        overlays.onMouseMove(x, y);
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
        if (overlays.onClick(x, y)) {
            return;
        }
        if (!game.getOffice().getCameras().isOpen() && Draw.inside(x, y, HIDE_X, HIDE_Y, HIDE_W, HIDE_H)) {
            game.toggleHide();
            return;
        }
        if (canUseDesk()) {
            if (Draw.inside(x, y, SAFE_X, SAFE_Y, SAFE_W, SAFE_H)) {
                overlays.open(DeskOverlays.Kind.SAFE);
                return;
            }
            if (Draw.inside(x, y, NOTE_X, NOTE_Y, NOTE_W, NOTE_H)) {
                overlays.open(DeskOverlays.Kind.JOURNAL);
                return;
            }
            if (Draw.inside(x, y, CUT_X, CUT_Y, CUT_W, CUT_H)) {
                overlays.open(DeskOverlays.Kind.ARTICLE);
                return;
            }
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

    /** Ce que le metteur en scène de la peur fait apparaître à l'écran. */
    private void onScare(TerrorDirector.Scare scare, double pan) {
        String side = pan < 0 ? "à gauche" : "à droite";
        switch (scare) {
            case FOOTSTEPS -> showSubtitle("* Des pas traînants, " + side + " *");
            case DOOR_CREAK -> showSubtitle("* Une porte grince, quelque part " + side + " *");
            case OBJECT_FALL -> {
                cupFallen = true;
                showSubtitle("* La tasse vient de tomber du bureau *");
            }
            case LIGHT_FLICKER -> lampOff = 1.6;
            case WARPED_MUSIC -> showSubtitle("* L'orgue joue tout seul, de travers *");
            case LITTLE_GIRL -> showSubtitle("* Une petite fille fredonne... tout près *");
            case DISTANT_LAUGH -> showSubtitle("* Un rire de femme, au loin, qui se déforme *");
            case SHADOW -> {
                shadowSide = pan < 0 ? Door.Side.LEFT : Door.Side.RIGHT;
                shadowTimer = 9;
            }
            case CAMERA_FACE -> {
                java.util.List<Room> rooms = Room.withCameras();
                faceRoom = rooms.get((int) (Math.random() * rooms.size()));
                faceTimer = 12;
            }
            default -> {
                // Silence et surtension : gérés par le son et par le disjoncteur.
            }
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
            officeShadow = Draw.render(W, H, this::paintOfficeShadow);
        }
        gc.drawImage(officeBackground, 0, 0);
        drawFuseBox(gc);
        drawSafe(gc);
        drawCup(gc);
        drawHallucinationOnWall(gc);
        if (noseTimer > 0) {
            gc.setFill(new RadialGradient(0, 0, NOSE_X, NOSE_Y, 26, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 40, 50, 0.95)), new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(NOSE_X - 26, NOSE_Y - 26, 52, 52);
            Draw.shadowText(gc, "POUET !", NOSE_X + 34, NOSE_Y + 30, assets.title(22), Draw.CREAM, TextAlignment.LEFT);
        }
        drawLeftDoor(gc);
        drawRightDoor(gc);
        drawDoorButton(gc, BTN_X, BTN_Y, game.getOffice().getLeftDoor().isClosed(), false, "Q");
        drawDoorButton(gc, RIGHT_BTN_X, RIGHT_BTN_Y, game.getOffice().getRightDoor().isClosed(),
                game.getOffice().getRightDoor().isBlocked(), "E");
        drawLightButton(gc, BTN_X, game.getOffice().getLeftLight().isOn(), "A");
        drawLightButton(gc, RIGHT_BTN_X, game.getOffice().getRightLight().isOn(), "D");
        Draw.grain(gc, 575, 444, 180, 100, 90, 0.35);
        // Pénombre : seule la lampe de bureau éclaire vraiment (et parfois, elle lâche)
        gc.drawImage(officeShadow, 0, 0);
        if (lampOff > 0) {
            Draw.shade(gc, Math.random() < 0.3 ? 0.35 : 0.62);
        }

        if (game.getPower().isPowerOut()) {
            drawBlackout(gc);
        }
        double flash = flash();
        if (flash > 0) {
            gc.setFill(Color.rgb(220, 230, 255, flash * 0.5));
            gc.fillRect(0, 0, W, H);
        }
        Draw.grain(gc, 280, 0.12);
        Ui.filmDamage(gc);
        if (flicker > 0) {
            Draw.shade(gc, flicker);
        }
    }

    /**
     * Pénombre de la régie : un voile noir, plus épais en haut, percé par les
     * portes (pour que les lampes des couloirs gardent leur effet) et réchauffé
     * autour de la lampe de bureau.
     */
    private void paintOfficeShadow(GraphicsContext g) {
        g.setFill(new LinearGradient(0, 0, 0, H, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, 0.62)), new Stop(0.55, Color.rgb(0, 0, 0, 0.32)),
                new Stop(1, Color.rgb(0, 0, 0, 0.45))));
        g.fillRect(0, 0, W, H);
        g.clearRect(DOOR_X, DOOR_Y, DOOR_W, DOOR_H);
        g.clearRect(RIGHT_DOOR_X, RIGHT_DOOR_Y, RIGHT_DOOR_W, RIGHT_DOOR_H);
        g.setFill(new RadialGradient(0, 0, 470, 540, 330, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 150, 80, 0.14)), new Stop(1, Color.TRANSPARENT)));
        g.fillOval(140, 210, 660, 660);        // Vignettage et lignes de balayage, dans la même image pour ne la dessiner qu'une fois
        Draw.scanlines(g, 0.05);
        Draw.vignette(g, 0.94);
    }

    /** Disjoncteur mural : voyant rouge et levier baissé quand un fusible a sauté. */
    private void drawFuseBox(GraphicsContext gc) {
        FuseBox fuses = game.getOffice().getFuseBox();
        boolean tripped = fuses.getTripped() != null;
        boolean hover = Draw.inside(mouseX, mouseY, FUSE_X, FUSE_Y, FUSE_W, FUSE_H) && tripped;
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(FUSE_X + 5, FUSE_Y + 6, FUSE_W, FUSE_H);
        gc.setFill(new LinearGradient(FUSE_X, 0, FUSE_X + FUSE_W, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3a3430")), new Stop(0.5, Color.web("#57504a")), new Stop(1, Color.web("#2e2926"))));
        gc.fillRect(FUSE_X, FUSE_Y, FUSE_W, FUSE_H);
        gc.setStroke(hover ? Draw.CREAM : Color.rgb(0, 0, 0, 0.7));
        gc.setLineWidth(hover ? 2 : 1);
        gc.strokeRect(FUSE_X + 0.5, FUSE_Y + 0.5, FUSE_W - 1, FUSE_H - 1);
        Ui.warningIcon(gc, FUSE_X + 16, FUSE_Y + 16, 8, Draw.AMBER);
        Draw.text(gc, "DANGER", FUSE_X + 30, FUSE_Y + 17, assets.ui(10), Draw.AMBER, TextAlignment.LEFT);
        // Voyant
        boolean blink = tripped && ((int) (time * 3)) % 2 == 0;
        gc.setFill(blink ? Color.web("#ff2a3a") : tripped ? Color.web("#5a0a10") : Color.web("#3a6a3a"));
        gc.fillOval(FUSE_X + FUSE_W - 22, FUSE_Y + 9, 12, 12);
        // Levier : en bas s'il a sauté, remonte pendant le réarmement
        double ratio = tripped ? fuses.getProgress() : 1.0;
        double leverY = FUSE_Y + 86 - 50 * ratio;
        gc.setFill(Color.web("#1a1614"));
        gc.fillRect(FUSE_X + FUSE_W / 2 - 4, FUSE_Y + 32, 8, 60);
        gc.setFill(tripped ? Color.web("#b01827") : Color.web("#8a7d70"));
        gc.fillRect(FUSE_X + FUSE_W / 2 - 18, leverY - 6, 36, 12);
        if (tripped) {
            Ui.keycap(gc, "F", FUSE_X + 8, FUSE_Y + FUSE_H - 14, assets.ui(11));
            if (fuses.isResetting()) {
                Ui.gauge(gc, FUSE_X + 30, FUSE_Y + FUSE_H - 18, FUSE_W - 38, 8, 6, fuses.getProgress(), Draw.AMBER);
            }
        }
    }

    /** Coffre du directeur, posé au sol à droite du bureau. */
    private void drawSafe(GraphicsContext gc) {
        boolean open = game.getOffice().getSafe().isOpen();
        boolean hover = Draw.inside(mouseX, mouseY, SAFE_X, SAFE_Y, SAFE_W, SAFE_H) && canUseDesk();
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRect(SAFE_X + 6, SAFE_Y + 6, SAFE_W, SAFE_H);
        gc.setFill(new LinearGradient(0, SAFE_Y, 0, SAFE_Y + SAFE_H, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3c3a36")), new Stop(1, Color.web("#1e1c1a"))));
        gc.fillRect(SAFE_X, SAFE_Y, SAFE_W, SAFE_H);
        gc.setStroke(hover ? Draw.CREAM : Color.rgb(176, 141, 69, 0.5));
        gc.setLineWidth(hover ? 2 : 1);
        gc.strokeRect(SAFE_X + 4, SAFE_Y + 4, SAFE_W - 8, SAFE_H - 8);
        double cx = SAFE_X + SAFE_W / 2;
        double cy = SAFE_Y + SAFE_H / 2;
        gc.setFill(Color.web("#8a7d70"));
        gc.fillOval(cx - 16, cy - 16, 32, 32);
        gc.setStroke(Color.web("#1a1614"));
        gc.setLineWidth(2);
        double a = time * 0.3;
        gc.strokeLine(cx, cy, cx + Math.cos(a) * 12, cy + Math.sin(a) * 12);
        if (open) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(SAFE_X + 8, SAFE_Y + 8, SAFE_W - 16, SAFE_H - 16);
            Draw.text(gc, "OUVERT", cx, cy, assets.ui(11), Draw.GOLD, TextAlignment.CENTER);
        }
        if (hover) {
            Draw.shadowText(gc, "COFFRE", cx, SAFE_Y - 12, assets.ui(13), Draw.CREAM, TextAlignment.CENTER);
        }
    }

    /** La tasse : sur le bureau, ou en miettes par terre si elle est tombée. */
    private void drawCup(GraphicsContext gc) {
        if (!cupFallen) {
            gc.setFill(Color.web("#d8cfc0"));
            gc.fillRoundRect(822, 528, 28, 30, 6, 6);
            gc.setStroke(Color.web("#d8cfc0"));
            gc.setLineWidth(3);
            gc.strokeOval(846, 534, 12, 14);
            gc.setFill(Color.web("#2a1a10"));
            gc.fillOval(822, 524, 28, 8);
            return;
        }
        gc.setFill(Color.web("#2a1a10"));
        gc.fillOval(800, 676, 70, 14);
        gc.setFill(Color.web("#cfc6b4"));
        double[][] shards = {{812, 672, 10, 6}, {838, 680, 14, 7}, {860, 670, 8, 5}, {826, 686, 6, 4}};
        for (double[] sh : shards) {
            gc.fillPolygon(new double[]{sh[0], sh[0] + sh[2], sh[0] + sh[2] * 0.4},
                    new double[]{sh[1], sh[1] + sh[3] * 0.3, sh[1] + sh[3]}, 3);
        }
    }

    /** Sous le bureau : presque rien à voir, seulement le bas de la pièce par une fente. */
    private void drawHidden(GraphicsContext gc) {
        Draw.clear(gc, Color.web("#050303"));
        // La fente de lumière entre le plateau et le sol
        gc.setFill(new LinearGradient(0, 470, 0, 640, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT), new Stop(0.5, Color.rgb(90, 60, 40, 0.35)),
                new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(160, 470, W - 320, 170);
        // Pieds du bureau
        gc.setFill(Color.web("#120a07"));
        gc.fillRect(140, 300, 60, 420);
        gc.fillRect(W - 200, 300, 60, 420);
        gc.fillRect(0, 0, W, 300);
        gc.setFill(new LinearGradient(0, 300, 0, 330, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1e130c")), new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, 300, W, 30);
        if (game.getPepin().isSearching()) {
            // Quelque chose passe devant la fente
            double px = (time * 260) % (W + 400) - 200;
            Draw.spriteAt(gc, assets.silhouette("pepin", "idle_1", 360), px, 690);
            Ui.alert(gc, "QUELQUE CHOSE FOUILLE LA RÉGIE... PAS UN BRUIT", W / 2, 120, assets.ui(16), time);
        } else {
            Draw.shadowText(gc, "Vous êtes caché sous le bureau.", W / 2, 150, assets.serif(26), Draw.TEXT,
                    TextAlignment.CENTER);
            Draw.text(gc, "Caché, vous ne pouvez rien utiliser. Pépin ne vous trouvera pas si vous restez silencieux.",
                    W / 2, 192, assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
            Draw.text(gc, "Les autres clowns, eux, entrent par les portes ouvertes.",
                    W / 2, 214, assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
        }
        Ui.hints(gc, W / 2, 252, TextAlignment.CENTER, assets.ui(14), "S", "Sortir de la cachette");
        Draw.grain(gc, 400, 0.18);
        Draw.overlay(gc, 0, 0.1, 0.95);
    }

    private boolean canUseDesk() {
        return game.getState() == GameState.PLAYING && !game.getOffice().getCameras().isOpen()
                && !game.getOffice().isHidden() && !game.getPower().isPowerOut() && !isFrozen();
    }

    /** Tout ce qui ne bouge jamais dans la régie, dessiné une seule fois. */
    private void paintOfficeBackground(GraphicsContext gc) {
        Draw.clear(gc, Color.web("#160d0f"));
        // Papier peint : larges rayures bordeaux et petits losanges dorés
        for (double x = 0; x < W; x += 80) {
            gc.setFill(Color.web("#3a0f16"));
            gc.fillRect(x, 0, 40, 440);
            gc.setFill(Color.web("#250a0e"));
            gc.fillRect(x + 40, 0, 40, 440);
            for (double y = 40; y < 430; y += 64) {
                Ui.diamond(gc, x + 60, y, 4, Color.rgb(201, 162, 74, 0.10));
            }
        }
        // Le papier peint pourrit : moisissure, coulures, lambeaux arrachés
        java.util.Random random = new java.util.Random(1031);
        for (int i = 0; i < 40; i++) {
            double r = 20 + random.nextDouble() * 80;
            double x = random.nextDouble() * W;
            double y = random.nextDouble() * 440;
            gc.setFill(new RadialGradient(0, 0, x, y, r, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(0, 0, 0, 0.32)), new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(x - r, y - r, r * 2, r * 2);
        }
        for (int i = 0; i < 24; i++) {
            double x = random.nextDouble() * W;
            double len = 60 + random.nextDouble() * 240;
            gc.setFill(new LinearGradient(0, 0, 0, len, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(25, 8, 4, 0.5)), new Stop(1, Color.TRANSPARENT)));
            gc.fillRect(x, 0, 2 + random.nextDouble() * 6, len);
        }
        for (int i = 0; i < 6; i++) {
            double x = random.nextDouble() * W;
            double y = 80 + random.nextDouble() * 300;
            double[] xs = new double[7];
            double[] ys = new double[7];
            for (int k = 0; k < 7; k++) {
                double a = k * Math.PI * 2 / 7;
                double rr = 12 + random.nextDouble() * 24;
                xs[k] = x + Math.cos(a) * rr * 1.4;
                ys[k] = y + Math.sin(a) * rr;
            }
            gc.setFill(Color.rgb(70, 54, 40, 0.5));
            gc.fillPolygon(xs, ys, 7);
        }
        gc.setFill(new LinearGradient(0, 0, 0, 160, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, 0.9)), new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, 0, W, 160);
        cobweb(gc, 0, 0, 1);
        cobweb(gc, W, 0, -1);
        // Mots griffonnés au mur par l'ancien veilleur
        gc.save();
        gc.translate(676, 72);
        gc.rotate(-2);
        Draw.text(gc, "ILS NE SONT JAMAIS PARTIS", 0, 0, assets.scrawl(18), Color.rgb(150, 14, 24, 0.55),
                TextAlignment.CENTER);
        gc.restore();
        gc.save();
        gc.translate(1236, 330);
        gc.rotate(-90);
        Draw.text(gc, "6 h... 6 h... 6 h...", 0, 0, assets.scrawl(12), Color.rgb(150, 14, 24, 0.4),
                TextAlignment.CENTER);
        gc.restore();
        // Cimaise dorée
        gc.setFill(Color.web("#5c4420"));
        gc.fillRect(0, 436, W, 9);
        gc.setFill(Color.web("#c9a24a"));
        gc.fillRect(0, 436, W, 2);
        // Lambris en bois
        gc.setFill(new LinearGradient(0, 445, 0, 590, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3b2418")), new Stop(1, Color.web("#1f120c"))));
        gc.fillRect(0, 445, W, 145);
        for (double x = 12; x < W; x += 124) {
            gc.setStroke(Color.rgb(0, 0, 0, 0.5));
            gc.setLineWidth(2);
            gc.strokeRect(x, 462, 104, 110);
            gc.setStroke(Color.rgb(255, 220, 160, 0.06));
            gc.setLineWidth(1);
            gc.strokeRect(x + 4, 466, 96, 102);
        }
        gc.setFill(Color.web("#0e0806"));
        gc.fillRect(0, 586, W, 8);
        paintFloor(gc);
        // Guirlande d'ampoules éteintes sous le plafond
        gc.setStroke(Color.rgb(20, 14, 10));
        gc.setLineWidth(2);
        for (double x0 = -40; x0 < W; x0 += 320) {
            double px = x0;
            double py = 14;
            for (double x = x0; x <= x0 + 320; x += 8) {
                double t = (x - x0) / 320;
                double y = 14 + 34 * 4 * t * (1 - t);
                gc.strokeLine(px, py, x, y);
                px = x;
                py = y;
            }
            for (int i = 1; i < 8; i++) {
                double t = i / 8.0;
                double bx = x0 + 320 * t;
                double by = 18 + 34 * 4 * t * (1 - t);
                if ((i + (int) x0) % 3 == 0) {
                    // Ampoule brisée : il ne reste que la douille
                    gc.setFill(Color.web("#1a1410"));
                    gc.fillRect(bx - 3, by - 4, 6, 5);
                } else {
                    Ui.bulb(gc, bx, by, false);
                }
            }
        }
        drawPoster(gc);
        drawClipping(gc);
        drawDesk(gc);
        paintControlPlate(gc, BTN_X);
        paintControlPlate(gc, RIGHT_BTN_X);
        // Lumière chaude de la lampe de bureau
        gc.setFill(new RadialGradient(0, 0, 470, 520, 420, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 190, 110, 0.18)), new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, 0, W, H);
    }

    /** Toile d'araignée dans un coin du plafond. */
    private static void cobweb(GraphicsContext gc, double x, double y, int dir) {
        gc.setStroke(Color.rgb(200, 190, 180, 0.13));
        gc.setLineWidth(1);
        for (int i = 0; i <= 6; i++) {
            double a = i * Math.PI / 12;
            gc.strokeLine(x, y, x + dir * Math.cos(a) * 150, y + Math.sin(a) * 150);
        }
        for (double r = 25; r <= 125; r += 25) {
            double px = x + dir * r;
            double py = y;
            for (int i = 1; i <= 6; i++) {
                double a = i * Math.PI / 12;
                double nx = x + dir * Math.cos(a) * r;
                double ny = y + Math.sin(a) * r;
                gc.strokeLine(px, py, (px + nx) / 2 + dir * 3, (py + ny) / 2 + 3);
                gc.strokeLine((px + nx) / 2 + dir * 3, (py + ny) / 2 + 3, nx, ny);
                px = nx;
                py = ny;
            }
        }
    }

    /** Sol en damier noir et bordeaux, en perspective. */
    private void paintFloor(GraphicsContext gc) {
        double top = 594;
        double vx = W / 2;
        double vy = 250;
        double[] rows = {top, 606, 622, 644, 674, 714, H + 40};
        for (int r = 0; r + 1 < rows.length; r++) {
            for (int c = -12; c < 12; c++) {
                double x0 = vx + c * 150;
                double x1 = vx + (c + 1) * 150;
                double ya = rows[r];
                double yb = rows[r + 1];
                double ka = (ya - vy) / (H + 40 - vy);
                double kb = (yb - vy) / (H + 40 - vy);
                gc.setFill((r + c) % 2 == 0 ? Color.web("#2a1612") : Color.web("#0d0807"));
                gc.fillPolygon(new double[]{vx + (x0 - vx) * ka, vx + (x1 - vx) * ka, vx + (x1 - vx) * kb, vx + (x0 - vx) * kb},
                        new double[]{ya, ya, yb, yb}, 4);
            }
        }
        gc.setFill(new LinearGradient(0, top, 0, H, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, 0.7)), new Stop(1, Color.rgb(0, 0, 0, 0.2))));
        gc.fillRect(0, top, W, H - top);
    }

    /** Plaque de commande en laiton, derrière la lampe et la porte d'un côté. */
    private void paintControlPlate(GraphicsContext gc, double bx) {
        double x = bx - 12;
        double y = LIGHT_Y - 14;
        double w = BTN_W + 24;
        double h = BTN_Y + BTN_H + 14 - y;
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(x + 5, y + 7, w, h, 12, 12);
        gc.setFill(new LinearGradient(x, 0, x + w, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6b5226")), new Stop(0.5, Color.web("#a8853f")), new Stop(1, Color.web("#5a441e"))));
        gc.fillRoundRect(x, y, w, h, 12, 12);
        gc.setFill(new LinearGradient(0, y, 0, y + h, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#262022")), new Stop(1, Color.web("#151112"))));
        gc.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 8, 8);
        gc.setStroke(Color.rgb(0, 0, 0, 0.6));
        gc.setLineWidth(1);
        gc.strokeLine(x + 12, BTN_Y - 7, x + w - 12, BTN_Y - 7);
        for (double[] screw : new double[][]{{x + 9, y + 9}, {x + w - 9, y + 9}, {x + 9, y + h - 9}, {x + w - 9, y + h - 9}}) {
            gc.setFill(Color.web("#d8c08a"));
            gc.fillOval(screw[0] - 2.5, screw[1] - 2.5, 5, 5);
        }
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
        } else if (lit && shadowSide == Door.Side.LEFT) {
            drawApparition(gc, DOOR_X + DOOR_W / 2, "bobo", "door", 430);
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
        } else if (!closed && lit && shadowSide == Door.Side.RIGHT) {
            drawApparition(gc, RIGHT_DOOR_X + RIGHT_DOOR_W / 2, "henry", "idle_2", 400);
        }
        if (game.getMimi().isAtEmbrasure()) {
            Draw.text(gc, "PORTE BLOQUÉE", RIGHT_DOOR_X + RIGHT_DOOR_W / 2,
                    RIGHT_DOOR_Y + 30, assets.ui(18), Color.web("#ff5260"), TextAlignment.CENTER);
        }
        drawFrame(gc, RIGHT_DOOR_X, RIGHT_DOOR_Y);
    }

    /**
     * Silhouette furtive au fond d'un couloir éclairé : elle disparaît une demi-seconde
     * après avoir été vue. Ce n'est pas un vrai clown, mais le joueur ne le sait pas.
     */
    private void drawApparition(GraphicsContext gc, double cx, String character, String frame, double height) {
        if (shadowTimer > 0.6) {
            shadowTimer = 0.6;
            pulse = 1.0;
        }
        gc.setGlobalAlpha(Math.min(0.85, shadowTimer * 1.6));
        Draw.spriteAt(gc, assets.silhouette(character, frame, height), cx + Draw.random(-2, 2), DOOR_Y + DOOR_H - 30);
        gc.setGlobalAlpha(1.0);
    }

    /** Encadrement de porte en bois sombre, avec moulure dorée. */
    private void drawFrame(GraphicsContext gc, double x, double y) {
        gc.setStroke(Color.web("#1d1411"));
        gc.setLineWidth(22);
        gc.strokeRect(x, y, DOOR_W, DOOR_H);
        gc.setStroke(Color.web("#3e2b1d"));
        gc.setLineWidth(6);
        gc.strokeRect(x - 8, y - 8, DOOR_W + 16, DOOR_H + 16);
        gc.setStroke(Color.rgb(201, 162, 74, 0.55));
        gc.setLineWidth(1.5);
        gc.strokeRect(x - 12, y - 12, DOOR_W + 24, DOOR_H + 24);
        gc.setStroke(Color.rgb(0, 0, 0, 0.6));
        gc.setLineWidth(2);
        gc.strokeRect(x + 10, y + 10, DOOR_W - 20, DOOR_H - 20);
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
        double cx = bx + BTN_W / 2;
        double cy = by + 44;
        boolean hover = Draw.inside(mouseX, mouseY, bx, by, BTN_W, BTN_H) && !game.getOffice().getCameras().isOpen();
        if (closed || blocked) {
            gc.setFill(new RadialGradient(0, 0, cx, cy, 64, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 50, 64, blocked ? 0.35 + 0.25 * Math.sin(time * 10) : 0.45)),
                    new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(cx - 64, cy - 64, 128, 128);
        }
        // Bague en laiton puis bouton bombé
        gc.setFill(new LinearGradient(0, cy - 32, 0, cy + 32, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#e2c98e")), new Stop(1, Color.web("#6b5226"))));
        gc.fillOval(cx - 32, cy - 32, 64, 64);
        gc.setFill(closed
                ? new RadialGradient(0, 0, cx - 8, cy - 10, 34, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#ff8a8f")), new Stop(0.45, Color.web("#e0303f")), new Stop(1, Color.web("#7a0d16")))
                : new RadialGradient(0, 0, cx - 8, cy - 10, 34, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#7a3a3f")), new Stop(0.5, Color.web("#4a1219")), new Stop(1, Color.web("#22070b"))));
        gc.fillOval(cx - 26, cy - 26, 52, 52);
        if (hover) {
            gc.setStroke(Draw.CREAM);
            gc.setLineWidth(2);
            gc.strokeOval(cx - 34, cy - 34, 68, 68);
        }
        String state = blocked ? "BLOQUÉE" : closed ? "FERMÉE" : "OUVERTE";
        Color stateColor = blocked ? Ui.ALERT : closed ? Color.web("#ff8a8f") : Draw.TEXT_DIM;
        Draw.text(gc, "PORTE", cx, by + 92, assets.ui(14), Draw.TEXT, TextAlignment.CENTER);
        Ui.keycap(gc, key, cx - 13, by + 112, assets.ui(12));
        Draw.text(gc, state, cx, by + 132, assets.ui(11), stateColor, TextAlignment.CENTER);
    }

    private void drawLightButton(GraphicsContext gc, double bx, boolean on, String key) {
        if (game.getOffice().getFuseBox().isTripped(FuseBox.Circuit.LIGHTS)) {
            Draw.text(gc, "GRILLÉ", bx + BTN_W / 2, LIGHT_Y + 24, assets.ui(13), Ui.ALERT, TextAlignment.CENTER);
            Draw.text(gc, "fusible", bx + BTN_W / 2, LIGHT_Y + 46, assets.ui(11), Draw.TEXT_DIM, TextAlignment.CENTER);
            return;
        }
        double cx = bx + BTN_W / 2;
        double cy = LIGHT_Y + 24;
        boolean hover = Draw.inside(mouseX, mouseY, bx, LIGHT_Y, BTN_W, LIGHT_H) && !game.getOffice().getCameras().isOpen();
        if (on) {
            gc.setFill(new RadialGradient(0, 0, cx, cy, 48, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 214, 130, 0.6)), new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(cx - 48, cy - 48, 96, 96);
        }
        gc.setFill(Color.web("#8a6d31"));
        gc.fillOval(cx - 19, cy - 19, 38, 38);
        gc.setFill(on
                ? new RadialGradient(0, 0, cx - 4, cy - 5, 18, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#fffbe6")), new Stop(0.5, Color.web("#ffd27a")), new Stop(1, Color.web("#c88a2a")))
                : new RadialGradient(0, 0, cx - 4, cy - 5, 18, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#6a5a3a")), new Stop(1, Color.web("#2a2218"))));
        gc.fillOval(cx - 15, cy - 15, 30, 30);
        if (hover) {
            gc.setStroke(Draw.CREAM);
            gc.setLineWidth(2);
            gc.strokeOval(cx - 22, cy - 22, 44, 44);
        }
        double kw = Ui.keycap(gc, key, bx + 8, LIGHT_Y + 55, assets.ui(11));
        Draw.text(gc, "LUMIÈRE", bx + 8 + kw + 5, LIGHT_Y + 55, assets.ui(11), on ? Draw.AMBER : Draw.TEXT_DIM,
                TextAlignment.LEFT);
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
        // Affiche vieillie : taches, éclaboussures de sang, coin arraché
        java.util.Random random = new java.util.Random(77);
        for (int i = 0; i < 16; i++) {
            double r = 4 + random.nextDouble() * 24;
            gc.setFill(Color.rgb(20, 6, 4, 0.2 + random.nextDouble() * 0.2));
            gc.fillOval(-125 + random.nextDouble() * 250 - r / 2, -160 + random.nextDouble() * 330 - r / 2, r, r);
        }
        gc.setFill(Color.rgb(110, 6, 14, 0.8));
        for (int i = 0; i < 9; i++) {
            double r = 2 + random.nextDouble() * 6;
            gc.fillOval(62 + random.nextDouble() * 48, -152 + random.nextDouble() * 56, r, r);
        }
        gc.fillRect(94, -118, 3, 64);
        gc.setFill(Color.web("#1a0c0e"));
        gc.fillPolygon(new double[]{125, 125, 72, 92}, new double[]{112, 170, 170, 142}, 4);
        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillRect(-125, -160, 250, 330);
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
        // Plateau en bois, en perspective, avec veinage
        double[] xs = {400, 900, 990, 310};
        double[] ys = {560, 560, H, H};
        gc.setFill(new LinearGradient(0, 560, 0, H, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a3122")), new Stop(1, Color.web("#1e130c"))));
        gc.fillPolygon(xs, ys, 4);
        gc.setStroke(Color.rgb(0, 0, 0, 0.25));
        gc.setLineWidth(1);
        for (int i = 1; i < 9; i++) {
            double t = i / 9.0;
            gc.strokeLine(400 + 500 * t, 560, 310 + 680 * t, H);
        }
        gc.setFill(Color.web("#6b4a30"));
        gc.fillRect(400, 554, 500, 8);
        gc.setFill(Color.rgb(255, 220, 170, 0.25));
        gc.fillRect(400, 554, 500, 2);
        // Moniteur cathodique, avec sa lueur verte
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(566, 436, 210, 132, 14, 14);
        gc.setFill(new LinearGradient(0, 428, 0, 562, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2e2a27")), new Stop(1, Color.web("#151312"))));
        gc.fillRoundRect(558, 428, 214, 134, 14, 14);
        gc.setFill(Color.web("#081109"));
        gc.fillRoundRect(574, 442, 182, 100, 18, 18);
        gc.setFill(new RadialGradient(0, 0, 665, 492, 110, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(90, 200, 110, 0.22)), new Stop(1, Color.TRANSPARENT)));
        gc.fillRoundRect(574, 442, 182, 100, 18, 18);
        Draw.text(gc, "CAM -- REC", 665, 486, assets.ui(14), Color.rgb(140, 220, 140, 0.8), TextAlignment.CENTER);
        Draw.text(gc, "MALVAUX SÉCURITÉ", 665, 506, assets.ui(9), Color.rgb(140, 220, 140, 0.45), TextAlignment.CENTER);
        gc.setFill(Color.web("#3a3532"));
        gc.fillRect(640, 562, 50, 6);
        gc.setFill(Color.rgb(90, 220, 110, 0.9));
        gc.fillOval(752, 548, 6, 6);
        // Lampe de bureau en laiton
        gc.setFill(Color.web("#2a1f14"));
        gc.fillOval(446, 552, 48, 10);
        gc.setFill(Color.web("#8a6d31"));
        gc.fillRect(467, 518, 6, 38);
        gc.setFill(new LinearGradient(440, 0, 500, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2a4a2e")), new Stop(0.5, Color.web("#3f6e45")), new Stop(1, Color.web("#1e3621"))));
        gc.fillPolygon(new double[]{436, 504, 490, 450}, new double[]{522, 522, 494, 494}, 4);
        gc.setFill(Color.rgb(255, 214, 150, 0.95));
        gc.fillOval(452, 518, 36, 8);
        // Carnet du veilleur (la tasse est dessinée à part : elle peut tomber)
        gc.save();
        gc.translate(360, 640);
        gc.rotate(-8);
        gc.setFill(Color.web("#bfb39a"));
        gc.fillRect(0, 0, 90, 60);
        gc.setStroke(Color.rgb(60, 50, 40, 0.5));
        gc.setLineWidth(1);
        for (double ly = 12; ly < 56; ly += 8) {
            gc.strokeLine(8, ly, 82, ly);
        }
        gc.restore();
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
        drawClue(gc, room);
        if (faceRoom == room && Math.random() < 0.85) {
            gc.setGlobalAlpha(0.38 + 0.12 * Math.sin(time * 9));
            Draw.spriteAt(gc, assets.sprite("henry", "scare_1", 620), W / 2 + 200, H + 80);
            gc.setGlobalAlpha(1.0);
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
            Ui.panel(gc, W / 2 - 200, H / 2 - 50, 400, 100, Ui.ALERT);
            Draw.text(gc, "SIGNAL PERDU", W / 2, H / 2 - 10, assets.title(40), Draw.CREAM, TextAlignment.CENTER);
            Draw.text(gc, "Reprise dans " + (int) Math.ceil(cameras.getDisabledTimeRemaining(room)) + " s",
                    W / 2, H / 2 + 26, assets.ui(14), Draw.TEXT_DIM, TextAlignment.CENTER);
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

        Ui.vhsTracking(gc, time);
        drawViewfinder(gc);
        Draw.text(gc, "31.10.94  " + game.getClock().getFormattedTime() + "  ▶ PLAY", 40, 108, assets.ui(14),
                Color.rgb(230, 225, 210, 0.7), TextAlignment.LEFT);
        Ui.panel(gc, 24, 20, 330, 70);
        if (((int) (time * 2)) % 2 == 0) {
            gc.setFill(Ui.ALERT);
            gc.fillOval(42, 36, 12, 12);
        }
        Draw.text(gc, "REC", 60, 42, assets.ui(14), Ui.ALERT, TextAlignment.LEFT);
        Draw.text(gc, room.getCameraLabel(), 336, 42, assets.ui(14), Draw.GOLD, TextAlignment.RIGHT);
        Draw.shadowText(gc, room.getDisplayName().toUpperCase(), 40, 70, assets.title(24), Draw.CREAM, TextAlignment.LEFT);
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(W / 2 - 170, 14, 340, 30, 15, 15);
        Ui.hints(gc, W / 2, 29, TextAlignment.CENTER, assets.ui(13), "1 - 8", "Caméra", "Gauche / Droite", "Défiler");
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
        boolean out = game.getPepin().isOut();
        Ui.panel(gc, ORGAN_X, ORGAN_Y, ORGAN_W, ORGAN_H, out ? Ui.ALERT : winding ? Draw.AMBER : Ui.EDGE);
        Draw.text(gc, "ORGUE DE BARBARIE", ORGAN_X + 20, ORGAN_Y + 24, assets.ui(15), Draw.CREAM, TextAlignment.LEFT);
        double ratio = organ.getCharge() / BarrelOrgan.MAX_CHARGE;
        Color level = Ui.levelColor(ratio);
        Draw.text(gc, (int) Math.ceil(organ.getCharge()) + " %", ORGAN_X + ORGAN_W - 20, ORGAN_Y + 24, assets.ui(15),
                level, TextAlignment.RIGHT);
        Ui.gauge(gc, ORGAN_X + 20, ORGAN_Y + 44, ORGAN_W - 40, 16, 12, ratio, level);

        // Manivelle qui tourne quand on la tient
        double cx = ORGAN_X + ORGAN_W - 44;
        double cy = ORGAN_Y + 100;
        double angle = winding ? time * 8 : 0.6;
        gc.setFill(Color.rgb(255, 255, 255, 0.05));
        gc.fillOval(cx - 26, cy - 26, 52, 52);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(4);
        gc.strokeLine(cx, cy, cx + Math.cos(angle) * 20, cy + Math.sin(angle) * 20);
        gc.setFill(Draw.GOLD);
        gc.fillOval(cx - 5, cy - 5, 10, 10);
        gc.setFill(Draw.CREAM);
        gc.fillOval(cx + Math.cos(angle) * 20 - 6, cy + Math.sin(angle) * 20 - 6, 12, 12);

        if (out) {
            Draw.text(gc, "PÉPIN EST SORTI ! " + (int) Math.ceil(game.getPepin().getEscapeTimeRemaining()) + " s",
                    ORGAN_X + 20, ORGAN_Y + 100, assets.ui(15), Ui.ALERT, TextAlignment.LEFT);
        } else if (winding) {
            Draw.text(gc, "Vous remontez l'orgue...", ORGAN_X + 20, ORGAN_Y + 100, assets.ui(15), Draw.AMBER,
                    TextAlignment.LEFT);
        } else {
            double kw = Ui.keycap(gc, "R", ORGAN_X + 20, ORGAN_Y + 100, assets.ui(13));
            Draw.text(gc, "maintenir pour remonter", ORGAN_X + 28 + kw, ORGAN_Y + 100, assets.ui(14), Draw.TEXT_DIM,
                    TextAlignment.LEFT);
        }
    }

    /** Chiffre du code du coffre, griffonné en rouge sur le mur d'une des salles. */
    private void drawClue(GraphicsContext gc, Room room) {
        int index = Safe.clueIndex(room);
        if (index < 0 || game.getOffice().getSafe().isOpen()) {
            return;
        }
        double[][] spots = {{250, 250, -8}, {980, 220, 6}, {300, 330, -4}};
        String text = new String[]{"I", "II", "III"}[index] + " = " + game.getOffice().getSafe().digit(index);
        gc.save();
        gc.translate(spots[index][0], spots[index][1]);
        gc.rotate(spots[index][2]);
        Draw.text(gc, text, 2, 3, assets.scrawl(34), Color.rgb(0, 0, 0, 0.6), TextAlignment.CENTER);
        Draw.text(gc, text, 0, 0, assets.scrawl(34), Color.rgb(176, 18, 31, 0.85), TextAlignment.CENTER);
        gc.restore();
    }

    /** Coins de viseur de caméra de surveillance. */
    private void drawViewfinder(GraphicsContext gc) {
        gc.setStroke(Color.rgb(232, 224, 208, 0.55));
        gc.setLineWidth(2);
        double m = 14;
        double l = 36;
        double[][] corners = {{m, m, 1, 1}, {W - m, m, -1, 1}, {m, H - m, 1, -1}, {W - m, H - m, -1, -1}};
        for (double[] c : corners) {
            gc.strokeLine(c[0], c[1], c[0] + c[2] * l, c[1]);
            gc.strokeLine(c[0], c[1], c[0], c[1] + c[3] * l);
        }
        gc.setStroke(Color.rgb(232, 224, 208, 0.18));
        gc.setLineWidth(1);
        gc.strokeLine(W / 2 - 14, H / 2, W / 2 + 14, H / 2);
        gc.strokeLine(W / 2, H / 2 - 14, W / 2, H / 2 + 14);
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
        if (mapBase == null) {
            // Fond du plan (cadre, titre, contour, couloirs) dessiné une seule fois
            mapBase = Draw.render(MAP_W, MAP_H, g -> {
                Ui.panel(g, 0, 0, MAP_W, MAP_H);
                Draw.text(g, "PLAN DU CHAPITEAU", MAP_W / 2, 17, assets.ui(13), Draw.GOLD, TextAlignment.CENTER);
                g.setStroke(Color.rgb(201, 162, 74, 0.25));
                g.setLineWidth(1);
                g.strokeLine(14, MAP_HEAD + 4, MAP_W - 14, MAP_HEAD + 4);
                g.setStroke(Color.rgb(255, 255, 255, 0.06));
                g.setLineWidth(2);
                g.strokeOval(14, MAP_HEAD + 14, MAP_W - 28, MAP_H - MAP_HEAD - 26);
                g.setStroke(Color.rgb(201, 162, 74, 0.35));
                g.setLineWidth(2);
                for (Room room : Room.values()) {
                    double[] a = MAP_POSITIONS.get(room);
                    for (Room next : game.getMap().neighbors(room)) {
                        if (room.ordinal() < next.ordinal()) {
                            double[] b = MAP_POSITIONS.get(next);
                            g.strokeLine(MAP_DX + a[0], MAP_DY + MAP_HEAD + a[1], MAP_DX + b[0], MAP_DY + MAP_HEAD + b[1]);
                        }
                    }
                }
            });
        }
        gc.drawImage(mapBase, MAP_X, MAP_Y);
        double top = MAP_Y + MAP_HEAD + MAP_DY;
        for (Room room : Room.values()) {
            double[] p = MAP_POSITIONS.get(room);
            double x = MAP_X + MAP_DX + p[0] - CAM_W / 2;
            double y = top + p[1] - CAM_H / 2;
            boolean selected = room == cameras.getSelected();
            boolean hover = room.hasCamera() && Draw.inside(mouseX, mouseY, x, y, CAM_W, CAM_H);
            if (selected) {
                gc.setFill(new RadialGradient(0, 0, x + CAM_W / 2, y + CAM_H / 2, 46, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 176, 0, 0.35)), new Stop(1, Color.TRANSPARENT)));
                gc.fillOval(x - 14, y - 32, CAM_W + 28, CAM_H + 64);
            }
            gc.setFill(room == Room.REGIE ? Color.web("#5a1019") : selected ? Draw.AMBER
                    : hover ? Color.web("#3a3032") : Color.web("#221b1c"));
            gc.fillRoundRect(x, y, CAM_W, CAM_H, 8, 8);
            gc.setStroke(selected ? Draw.CREAM : room == Room.REGIE ? Ui.ALERT : Color.rgb(201, 162, 74, 0.5));
            gc.setLineWidth(selected ? 2 : 1);
            gc.strokeRoundRect(x, y, CAM_W, CAM_H, 8, 8);
            String label = room == Room.REGIE ? "VOUS" : room.getCameraLabel();
            Draw.text(gc, label, x + CAM_W / 2, y + CAM_H / 2, assets.ui(12),
                    selected ? Color.BLACK : Draw.TEXT, TextAlignment.CENTER);
            if (room.hasCamera() && cameras.isDisabled(room)) {
                gc.setFill(Ui.ALERT);
                gc.fillOval(x + CAM_W - 8, y - 3, 8, 8);
            }
        }
    }

    private Optional<Room> cameraAt(double x, double y) {
        for (Room room : Room.withCameras()) {
            double[] p = MAP_POSITIONS.get(room);
            if (Draw.inside(x, y, MAP_X + MAP_DX + p[0] - CAM_W / 2, MAP_Y + MAP_HEAD + MAP_DY + p[1] - CAM_H / 2,
                    CAM_W, CAM_H)) {
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

    /** Onglet de la tablette, en bas de l'écran, comme une tablette qu'on tire vers soi. */
    private void drawTabletBar(GraphicsContext gc) {
        boolean open = game.getOffice().getCameras().isOpen();
        boolean dark = game.getPower().isPowerOut();
        boolean hover = Draw.inside(mouseX, mouseY, BAR_X, BAR_Y, BAR_W, BAR_H) && !dark;
        double inset = 26;
        double[] xs = {BAR_X, BAR_X + inset, BAR_X + BAR_W - inset, BAR_X + BAR_W};
        double[] ys = {H + 2, BAR_Y, BAR_Y, H + 2};
        gc.setFill(hover ? Color.rgb(60, 14, 20, 0.92) : Ui.PANEL);
        gc.fillPolygon(xs, ys, 4);
        gc.setStroke(dark ? Ui.ALERT : open || hover ? Draw.AMBER : Ui.EDGE);
        gc.setLineWidth(1.5);
        gc.strokePolyline(xs, ys, 4);
        double cy = BAR_Y + BAR_H / 2 + 2;
        if (dark) {
            Draw.text(gc, "PLUS DE COURANT", W / 2, cy, assets.ui(16), Ui.ALERT, TextAlignment.CENTER);
            return;
        }
        if (!open && game.getOffice().getFuseBox().isTripped(FuseBox.Circuit.CAMERAS)) {
            Draw.text(gc, "CAMÉRAS HORS SERVICE (FUSIBLE)", W / 2, cy, assets.ui(15), Ui.ALERT, TextAlignment.CENTER);
            return;
        }
        if (!open && game.getOffice().isHidden()) {
            Draw.text(gc, "SORTEZ DE LA CACHETTE (S)", W / 2, cy, assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
            return;
        }
        // Chevron : vers le haut pour lever, vers le bas pour baisser
        double chx = BAR_X + 60;
        int dir = open ? 1 : -1;
        gc.setStroke(open ? Draw.AMBER : Draw.CREAM);
        gc.setLineWidth(2.5);
        gc.strokeLine(chx - 9, cy - dir * 5, chx, cy + dir * 4);
        gc.strokeLine(chx, cy + dir * 4, chx + 9, cy - dir * 5);
        gc.strokeLine(BAR_X + BAR_W - 69, cy - dir * 5, BAR_X + BAR_W - 60, cy + dir * 4);
        gc.strokeLine(BAR_X + BAR_W - 60, cy + dir * 4, BAR_X + BAR_W - 51, cy - dir * 5);
        String label = open ? "BAISSER" : "CAMÉRAS";
        double lw = Ui.width(label, assets.ui(17));
        double kw = Ui.width("Espace", assets.ui(12)) + 12;
        double start = W / 2 - (lw + 10 + kw) / 2;
        Draw.text(gc, label, start, cy, assets.ui(17), open ? Draw.AMBER : Draw.CREAM, TextAlignment.LEFT);
        Ui.keycap(gc, "Espace", start + lw + 10, cy, assets.ui(12));
    }

    private void drawHud(GraphicsContext gc) {
        drawClock(gc);
        drawPower(gc);
        if (timeScale > 1) {
            Ui.panel(gc, W - 246, 106, 222, 30, Draw.AMBER);
            Draw.text(gc, "TEMPS x" + (int) timeScale + "  (F2)", W - 135, 121, assets.ui(14), Draw.AMBER,
                    TextAlignment.CENTER);
        }

        // Alertes en haut de l'écran, visibles depuis la régie comme depuis les caméras
        double alertY = 76;
        if (game.getMimi().isAtEmbrasure()) {
            int seconds = (int) Math.ceil(game.getMimi().getEmbrasureTimeRemaining());
            Ui.alert(gc, "MIMI BLOQUE LA PORTE - REGARDEZ LA CAM 7 (" + seconds + " s)", W / 2, alertY,
                    assets.ui(15), time);
            alertY += 44;
        }
        BarrelOrgan organ = game.getOffice().getOrgan();
        boolean onOrganCamera = game.getOffice().getCameras().isWatching(Room.FOSSE_ORCHESTRE);
        if (!game.getPower().isPowerOut() && !onOrganCamera && organ.getCharge() < ORGAN_WARNING
                && !game.getOffice().isHidden()) {
            String text = game.getPepin().isOut() ? "PÉPIN EST SORTI - CAM 8 POUR L'ORGUE, OU CACHEZ-VOUS (S)"
                    : "L'ORGUE FAIBLIT (" + (int) Math.ceil(organ.getCharge()) + " %) - REMONTEZ-LE SUR LA CAM 8";
            Ui.alert(gc, text, W / 2, alertY, assets.ui(15), time);
            alertY += 44;
        }
        if (game.getOffice().getFuseBox().getTripped() != null && !game.getPower().isPowerOut()) {
            Ui.alert(gc, "UN FUSIBLE A SAUTÉ - MAINTENEZ F (OU LE LEVIER) SUR LE DISJONCTEUR", W / 2, alertY,
                    assets.ui(15), time);
        }
        drawNoise(gc);
        if (!game.getOffice().getCameras().isOpen() && !game.getPower().isPowerOut() && !isFrozen()) {
            boolean hidden = game.getOffice().isHidden();
            boolean hover = Draw.inside(mouseX, mouseY, HIDE_X, HIDE_Y, HIDE_W, HIDE_H);
            Ui.panel(gc, HIDE_X, HIDE_Y, HIDE_W, HIDE_H, hover ? Draw.GOLD : Ui.EDGE);
            double kw = Ui.keycap(gc, "S", HIDE_X + 14, HIDE_Y + HIDE_H / 2, assets.ui(12));
            Draw.text(gc, hidden ? "SORTIR" : "SE CACHER", HIDE_X + 24 + kw, HIDE_Y + HIDE_H / 2, assets.ui(14),
                    hover ? Draw.CREAM : Draw.TEXT, TextAlignment.LEFT);
        }

        if (subtitleTimer > 0 && subtitle != null) {
            gc.setGlobalAlpha(Math.min(1.0, subtitleTimer));
            double w = Ui.width(subtitle, assets.ui(17)) + 50;
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRoundRect(W / 2 - w / 2, 622, w, 34, 17, 17);
            Draw.text(gc, subtitle, W / 2, 639, assets.ui(17), Draw.CREAM, TextAlignment.CENTER);
            gc.setGlobalAlpha(1.0);
        }
    }

    /** Bruit fait dans la régie : plus il est haut, plus les clowns vous entendent. */
    private void drawNoise(GraphicsContext gc) {
        double level = game.getOffice().getNoise().getLevel() / NoiseMeter.MAX;
        double x = 24;
        double y = H - 150;
        Draw.text(gc, "BRUIT", x + 18, y, assets.ui(12), Draw.TEXT_DIM, TextAlignment.LEFT);
        Ui.gauge(gc, x + 70, y - 5, 230, 10, 12, level,
                game.getOffice().getNoise().isLoud() ? Ui.ALERT : level > 0.25 ? Draw.AMBER : Draw.TEXT_DIM);
    }

    /** Heure, nuit et avancée de la nuit, en haut à droite. */
    private void drawClock(GraphicsContext gc) {
        double x = W - 246;
        double y = 16;
        Ui.panel(gc, x, y, 222, 82);
        Draw.shadowText(gc, game.getClock().getFormattedTime(), x + 206, y + 34, assets.title(38), Draw.CREAM,
                TextAlignment.RIGHT);
        String night = game.isCustomNight() ? "NUIT PERSO" : "NUIT " + game.getCurrentNight();
        Draw.text(gc, night, x + 18, y + 26, assets.ui(14), Draw.GOLD, TextAlignment.LEFT);
        Draw.text(gc, "jusqu'à 6 h", x + 18, y + 44, assets.ui(11), Draw.TEXT_DIM, TextAlignment.LEFT);
        int hour = game.getClock().getHour();
        for (int i = 0; i < 6; i++) {
            double px = x + 24 + i * 35;
            Ui.diamond(gc, px, y + 66, i < hour ? 5 : 3, i < hour ? Draw.GOLD : Color.rgb(255, 255, 255, 0.18));
            if (i < 5) {
                gc.setStroke(Color.rgb(255, 255, 255, i < hour - 1 ? 0.35 : 0.08));
                gc.setLineWidth(1);
                gc.strokeLine(px + 7, y + 66, px + 28, y + 66);
            }
        }
    }

    /** Énergie restante et consommation, en bas à gauche. */
    private void drawPower(GraphicsContext gc) {
        double x = 24;
        double y = H - 118;
        boolean dark = game.getPower().isPowerOut();
        double energy = game.getPower().getEnergy();
        double ratio = energy / 100.0;
        Color level = Ui.levelColor(ratio);
        Ui.panel(gc, x, y, 300, 102, dark ? Ui.ALERT : Ui.EDGE);
        Draw.text(gc, "ÉNERGIE", x + 18, y + 20, assets.ui(12), Draw.TEXT_DIM, TextAlignment.LEFT);
        Draw.text(gc, "CONSOMMATION", x + 282, y + 20, assets.ui(12), Draw.TEXT_DIM, TextAlignment.RIGHT);
        boolean lowBlink = energy < 15 && !dark && ((int) (time * 3)) % 2 == 0;
        Ui.battery(gc, x + 18, y + 36, 52, 26, ratio, lowBlink ? Color.TRANSPARENT : level);
        Draw.shadowText(gc, (int) Math.ceil(energy) + " %", x + 86, y + 50, assets.title(30), level, TextAlignment.LEFT);
        int usage = dark ? 0 : game.getPower().computeUsage();
        Ui.gauge(gc, x + 186, y + 42, 96, 14, 6, usage / 6.0, usage <= 1 ? Ui.OK : usage <= 3 ? Draw.AMBER : Ui.ALERT);
        Ui.gauge(gc, x + 18, y + 78, 264, 8, 20, ratio, level);
    }

    private void drawTitleCard(GraphicsContext gc) {
        double alpha = cassetteActive ? 1.0 : Math.min(1.0, titleCard / 1.0);
        gc.setGlobalAlpha(alpha);
        Draw.clear(gc, Color.BLACK);
        gc.setFill(new RadialGradient(0, 0, W / 2, H / 2 - 20, 420, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(110, 14, 26, 0.35)), new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, 0, W, H);
        int night = game.getCurrentNight();
        gc.setStroke(Color.rgb(201, 162, 74, 0.6));
        gc.setLineWidth(1);
        gc.strokeLine(W / 2 - 160, H / 2 - 110, W / 2 - 60, H / 2 - 110);
        gc.strokeLine(W / 2 + 60, H / 2 - 110, W / 2 + 160, H / 2 - 110);
        Ui.diamond(gc, W / 2 - 166, H / 2 - 110, 4, Draw.GOLD);
        Ui.diamond(gc, W / 2 + 166, H / 2 - 110, 4, Draw.GOLD);
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
        Ui.hints(gc, W / 2, H - 50, TextAlignment.CENTER, assets.ui(14), "Entrée", "Passer");
        gc.setGlobalAlpha(1.0);
    }

    /**
     * La cassette du veilleur : un vieux magnétophone posé sur le bureau, dans
     * le noir, qui déroule la voix de Marcel en sous-titres. Au milieu, la bande
     * se dérègle et murmure autre chose.
     */
    private void drawCassette(GraphicsContext gc) {
        if (cassetteBackground == null) {
            cassetteBackground = Draw.render(W, H, this::paintCassetteBackground);
        }
        boolean glitch = glitchTimer > 0;
        double shake = glitch ? Draw.random(-6, 6) : 0;
        gc.drawImage(cassetteBackground, Math.round(shake), 0);

        int total = Math.max(1, cassetteLength());
        double progress = Math.min(1.0, typedChars / (double) total);
        drawTapeDeck(gc, W / 2 + shake, 70, progress, glitch);

        // Sous-titres : les dernières lignes, les plus anciennes s'effacent
        List<String> shownLines = new ArrayList<>();
        int remaining = typedChars;
        for (String line : cassetteLines) {
            if (remaining <= 0) {
                break;
            }
            shownLines.add(line.substring(0, Math.min(line.length(), remaining)));
            remaining -= line.length() + 1;
        }
        int first = Math.max(0, shownLines.size() - 4);
        double y = 470;
        Ui.smear(gc, W / 2, 520, 1180, 230, 0.9);
        Draw.text(gc, "MARCEL, LE VEILLEUR D'AVANT", W / 2, 432, assets.serif(17), Draw.GOLD, TextAlignment.CENTER);
        for (int i = first; i < shownLines.size(); i++) {
            double age = shownLines.size() - 1 - i;
            Color color = Color.rgb(230, 220, 196, Math.max(0.35, 1.0 - age * 0.22));
            Draw.shadowText(gc, shownLines.get(i), W / 2, y, assets.ui(22), color, TextAlignment.CENTER);
            y += 34;
        }

        if (glitch) {
            // La bande murmure autre chose
            String whisper = TAPE_WHISPERS[Math.min(game.getCurrentNight(), TAPE_WHISPERS.length) - 1];
            gc.setFill(Color.rgb(120, 0, 10, 0.25 + Math.random() * 0.2));
            gc.fillRect(0, 0, W, H);
            Ui.vhsTracking(gc, time * 6);
            Draw.grain(gc, 1800, 0.45);
            gc.save();
            gc.translate(W / 2 + Draw.random(-8, 8), 330 + Draw.random(-4, 4));
            gc.rotate(Draw.random(-3, 3));
            Ui.ghostText(gc, whisper, 0, 0, assets.scrawl(46), Color.rgb(220, 20, 34), TextAlignment.CENTER, 6);
            gc.restore();
        } else {
            Ui.vhsTracking(gc, time);
        }

        String action = typedChars < cassetteLength() ? "Tout afficher" : "Commencer la nuit";
        Ui.hints(gc, W / 2, H - 34, TextAlignment.CENTER, assets.ui(14), "Entrée", action);
        Draw.grain(gc, 500, 0.15);
        Ui.filmDamage(gc);
        Draw.overlay(gc, 0, 0.2, 0.95);
    }

    /** Fond de la cassette : le couloir flou et sombre, et le bureau sous la lampe. */
    private void paintCassetteBackground(GraphicsContext g) {
        Draw.clear(g, Color.BLACK);
        g.setEffect(new javafx.scene.effect.GaussianBlur(24));
        Draw.cover(g, assets.camera(Room.COULOIR_GAUCHE), 0, 0, W, H, 1.1);
        g.setEffect(null);
        g.setFill(Color.rgb(8, 2, 3, 0.8));
        g.fillRect(0, 0, W, H);
        // Plateau du bureau
        g.setFill(new LinearGradient(0, 380, 0, H, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2e1d14")), new Stop(1, Color.web("#0c0705"))));
        g.fillPolygon(new double[]{150, W - 150, W + 60, -60}, new double[]{380, 380, H, H}, 4);
        g.setFill(Color.rgb(255, 220, 170, 0.12));
        g.fillRect(150, 378, W - 300, 3);
        // Lumière de la lampe qui tombe sur le magnétophone
        g.setFill(new RadialGradient(0, 0, W / 2, 240, 460, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 170, 100, 0.20)), new Stop(1, Color.TRANSPARENT)));
        g.fillRect(0, 0, W, H);
    }

    /** Magnétophone à bobines : bandes qui défilent, vumètre, compteur et touches. */
    private void drawTapeDeck(GraphicsContext gc, double cx, double top, double progress, boolean glitch) {
        double w = 600;
        double h = 290;
        double x = cx - w / 2;
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(x + 10, top + 16, w, h, 18, 18);
        gc.setFill(new LinearGradient(0, top, 0, top + h, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2c2724")), new Stop(1, Color.web("#151210"))));
        gc.fillRoundRect(x, top, w, h, 18, 18);
        gc.setStroke(Color.rgb(176, 141, 69, 0.45));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x + 1, top + 1, w - 2, h - 2, 18, 18);
        Draw.text(gc, "MALVAUX  AUDIO  -  R7", x + 24, top + 22, assets.serif(14), Draw.GOLD, TextAlignment.LEFT);

        // Les deux bobines : la bande passe de gauche à droite
        double reelY = top + 112;
        double[] reelX = {cx - 150, cx + 150};
        double[] tape = {62 - 32 * progress, 30 + 32 * progress};
        gc.setStroke(Color.web("#3a2a1e"));
        gc.setLineWidth(2);
        gc.strokeLine(reelX[0], reelY + tape[0], cx - 40, top + 186);
        gc.strokeLine(cx - 40, top + 186, cx + 40, top + 186);
        gc.strokeLine(cx + 40, top + 186, reelX[1], reelY + tape[1]);
        for (int i = 0; i < 2; i++) {
            gc.setFill(Color.web("#0d0b0a"));
            gc.fillOval(reelX[i] - 76, reelY - 76, 152, 152);
            gc.setFill(Color.web("#3a2a1e"));
            gc.fillOval(reelX[i] - tape[i], reelY - tape[i], tape[i] * 2, tape[i] * 2);
            gc.setStroke(Color.rgb(200, 190, 170, 0.35));
            gc.setLineWidth(1.5);
            gc.strokeOval(reelX[i] - 74, reelY - 74, 148, 148);
            gc.setStroke(Draw.CREAM);
            gc.setLineWidth(4);
            for (int k = 0; k < 3; k++) {
                double a = reelAngle + k * Math.PI * 2 / 3;
                gc.strokeLine(reelX[i] + Math.cos(a) * 9, reelY + Math.sin(a) * 9,
                        reelX[i] + Math.cos(a) * 26, reelY + Math.sin(a) * 26);
            }
            gc.setFill(Color.web("#8a7d70"));
            gc.fillOval(reelX[i] - 8, reelY - 8, 16, 16);
        }

        // Étiquette griffonnée
        gc.setFill(Color.web("#d8cfb8"));
        gc.fillRect(cx - 76, top + 50, 152, 40);
        Draw.text(gc, "Nuit " + game.getCurrentNight() + " - Marcel", cx, top + 70, assets.scrawl(14),
                Color.web("#2a1810"), TextAlignment.CENTER);

        // Vumètre
        double vx = x + 28;
        double vy = top + 214;
        gc.setFill(Color.web("#d9cba0"));
        gc.fillRect(vx, vy, 120, 56);
        gc.setStroke(Color.web("#3a2a1e"));
        gc.setLineWidth(1);
        for (int k = 0; k <= 8; k++) {
            double a = Math.toRadians(-140 + k * 12.5);
            gc.strokeLine(vx + 60 + Math.cos(a) * 40, vy + 54 + Math.sin(a) * 40,
                    vx + 60 + Math.cos(a) * 46, vy + 54 + Math.sin(a) * 46);
        }
        double level = glitch ? 0.95 + Draw.random(-0.05, 0.05)
                : typedChars < cassetteLength() ? 0.35 + 0.35 * Math.abs(Math.sin(time * 11)) * Math.random()
                : 0.05;
        double needle = Math.toRadians(-140 + level * 100);
        gc.setStroke(level > 0.85 ? Draw.RED : Color.web("#1a1410"));
        gc.setLineWidth(2);
        gc.strokeLine(vx + 60, vy + 54, vx + 60 + Math.cos(needle) * 44, vy + 54 + Math.sin(needle) * 44);
        Draw.text(gc, "VU", vx + 108, vy + 46, assets.ui(10), Color.web("#3a2a1e"), TextAlignment.CENTER);

        // Compteur mécanique
        String counter = String.format("%03d", typedChars / 3);
        for (int k = 0; k < 3; k++) {
            gc.setFill(Color.web("#0b0a09"));
            gc.fillRect(cx - 45 + k * 30, top + 222, 26, 36);
            Draw.text(gc, counter.substring(k, k + 1), cx - 32 + k * 30, top + 240, assets.ui(22), Draw.CREAM,
                    TextAlignment.CENTER);
        }

        // Touches : lecture enfoncée, voyant rouge
        String[] keys = {"<<", ">", "[]", ">>"};
        for (int k = 0; k < keys.length; k++) {
            double bx = x + w - 230 + k * 50;
            boolean pressed = k == 1;
            gc.setFill(pressed ? Color.web("#6a1018") : Color.web("#4a433d"));
            gc.fillRect(bx, top + (pressed ? 226 : 220), 42, 32);
            if (pressed) {
                gc.setStroke(Color.rgb(255, 80, 90, 0.6));
                gc.setLineWidth(1);
                gc.strokeRect(bx + 0.5, top + 226.5, 41, 31);
            }
            Draw.text(gc, keys[k], bx + 21, top + (pressed ? 242 : 236), assets.ui(13), Draw.CREAM, TextAlignment.CENTER);
        }
        boolean led = glitch ? Math.random() < 0.5 : ((int) (time * 1.5)) % 2 == 0;
        gc.setFill(led ? Color.web("#ff2a3a") : Color.web("#4a0a10"));
        gc.fillOval(x + w - 34, top + 14, 12, 12);
        Draw.text(gc, glitch ? "ERREUR" : "LECTURE", x + w - 42, top + 21, assets.ui(11),
                glitch ? Ui.ALERT : Draw.TEXT_DIM, TextAlignment.RIGHT);
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
        boolean hover = Draw.inside(mouseX, mouseY, PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H);
        Ui.panel(gc, PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H, hover ? Draw.GOLD : Ui.EDGE);
        double cy = PAUSE_Y + PAUSE_H / 2;
        double kw = Ui.keycap(gc, "P", PAUSE_X + 20, cy, assets.ui(12));
        gc.setFill(hover ? Draw.CREAM : Draw.TEXT);
        gc.fillRect(PAUSE_X + 34 + kw, cy - 7, 4, 14);
        gc.fillRect(PAUSE_X + 42 + kw, cy - 7, 4, 14);
        Draw.text(gc, "PAUSE", PAUSE_X + 56 + kw, cy, assets.ui(14), hover ? Draw.CREAM : Draw.TEXT, TextAlignment.LEFT);
    }

    private void drawPauseOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, W, H);
        Ui.window(gc, PAUSE_PANEL_X, PAUSE_PANEL_Y, PAUSE_PANEL_W, PAUSE_PANEL_H, "PAUSE", assets.title(40));
        Draw.text(gc, (game.isCustomNight() ? "Nuit personnalisée" : "Nuit " + game.getCurrentNight()) + "  -  "
                + game.getClock().getFormattedTime(), W / 2, PAUSE_PANEL_Y + 92, assets.ui(15), Draw.TEXT_DIM,
                TextAlignment.CENTER);
        drawPauseAction(gc, RESUME_Y, "REPRENDRE");
        drawPauseAction(gc, MENU_Y, "RETOUR AU MENU");
        Ui.hints(gc, W / 2, PAUSE_PANEL_Y + PAUSE_PANEL_H - 34, TextAlignment.CENTER, assets.ui(13),
                "P / Échap", "Reprendre");
    }

    private void drawPauseAction(GraphicsContext gc, double y, String label) {
        boolean hover = Draw.inside(mouseX, mouseY, RESUME_X, y, PAUSE_ACTION_W, PAUSE_ACTION_H);
        Ui.button(gc, RESUME_X, y, PAUSE_ACTION_W, PAUSE_ACTION_H, label, assets.ui(18), hover);
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
