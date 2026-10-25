package com.epitech.fnah.view;

import com.epitech.fnah.model.Room;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;

/**
 * Prologue cinématographique joué au lancement (et depuis le menu) : la
 * dernière représentation de 1994, l'incendie, le journal du lendemain,
 * l'annonce d'aujourd'hui, puis quelque chose au fond du couloir, et le titre.
 *
 * <p>Chaque plan a ses images, ses sons et son texte tapé à la machine.
 * Le joueur peut passer le prologue à tout moment.</p>
 */
final class IntroScreen implements Screen {

    /** Un plan : début, fin, image de fond et texte. */
    private record Shot(double start, double end, Room background, String text) {
    }

    private static final Shot[] SHOTS = {
        new Shot(0.0, 7.5, Room.PISTE_CENTRALE, "31 octobre 1994. Dernière représentation du Cirque Malvaux."),
        new Shot(7.5, 15.0, Room.PISTE_CENTRALE, "Pendant le spectacle, le feu prend dans les loges. "
                + "Le public s'enfuit. La troupe, elle, ne ressort jamais."),
        new Shot(15.0, 20.0, null, ""),
        new Shot(20.0, 28.0, Room.COULOIR_GAUCHE, "Trente ans plus tard, la famille Malvaux cherche un veilleur de nuit. "
                + "Un seul poste. De minuit à six heures."),
        new Shot(28.0, 33.5, Room.COULOIR_GAUCHE, "La régie est au bout du couloir. Ne laissez rien entrer."),
        new Shot(33.5, 40.0, null, ""),
    };
    private static final double DURATION = 40.0;
    private static final double TYPE_SPEED = 32;
    private static final double SHADOW_AT = 30.6;

    private final Assets assets;
    private final SoundManager sound;
    private final Runnable onFinished;
    private double time;
    private int shot = -1;
    private boolean finished;

    IntroScreen(Assets assets, SoundManager sound, Runnable onFinished) {
        this.assets = assets;
        this.sound = sound;
        this.onFinished = onFinished;
    }

    @Override
    public void onEnter() {
        time = 0;
        shot = -1;
        finished = false;
    }

    @Override
    public void update(double dt) {
        if (finished) {
            return;
        }
        double before = time;
        time += dt;
        int current = shotAt(time);
        if (current != shot) {
            shot = current;
            startShot(shot);
        }
        if (before < SHADOW_AT && time >= SHADOW_AT) {
            sound.clownLaugh();
        }
        // Frappes de machine à écrire, pas trop serrées
        Shot s = SHOTS[shot];
        int typedBefore = typed(s, before);
        int typedNow = typed(s, time);
        if (typedNow > typedBefore && typedNow % 3 == 0 && typedNow < s.text().length()) {
            sound.typewriter();
        }
        if (time >= DURATION) {
            finish();
        }
    }

    private void startShot(int index) {
        switch (index) {
            case 0 -> sound.introAmbience(true, false);
            case 1 -> sound.introAmbience(true, true);
            case 2 -> {
                sound.introAmbience(false, false);
                sound.thud();
            }
            case 3 -> sound.introAmbience(true, false);
            case 5 -> {
                sound.introAmbience(false, false);
                sound.thud();
                sound.musicBox();
            }
            default -> {
                // Pas de nouveau son : le plan continue l'ambiance du précédent.
            }
        }
    }

    private void finish() {
        if (!finished) {
            finished = true;
            sound.endIntro();
            onFinished.run();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        Draw.clear(gc, Color.BLACK);
        if (shot < 0) {
            return;
        }
        Shot s = SHOTS[shot];
        double local = time - s.start();
        double length = s.end() - s.start();
        double fade = Math.min(1.0, Math.min(local / 1.2, (length - local) / 0.8));
        fade = Math.max(0, fade);

        switch (shot) {
            case 0, 1 -> drawRing(gc, local, fade, shot == 1);
            case 2 -> drawHeadline(gc, local);
            case 3, 4 -> drawCorridor(gc, local, fade, shot == 4);
            default -> drawTitle(gc, local);
        }

        if (!s.text().isEmpty()) {
            String shown = s.text().substring(0, typed(s, time));
            var lines = NightScreen.wrap(shown, 70);
            double y = Draw.H - 120 - (lines.size() - 1) * 16;
            Ui.smear(gc, Draw.W / 2, y + (lines.size() - 1) * 16, 1100, 60 + lines.size() * 34, 0.85);
            for (String line : lines) {
                Draw.shadowText(gc, line, Draw.W / 2, y, assets.ui(22), Draw.CREAM, TextAlignment.CENTER);
                y += 34;
            }
        }
        Draw.grain(gc, 500, 0.2);
        Ui.filmDamage(gc);
        Draw.overlay(gc, 0, 0.18, 0.95);
        if (time > 0.5) {
            Ui.hints(gc, Draw.W - 30, Draw.H - 24, TextAlignment.RIGHT, assets.ui(13), "Entrée", "Passer");
        }
    }

    /** La piste, de plus en plus proche ; pendant l'incendie, tout vire au rouge et tremble. */
    private void drawRing(GraphicsContext gc, double local, double fade, boolean fire) {
        double zoom = 1.0 + (shot == 0 ? local : 7.5 + local) * 0.012;
        gc.setGlobalAlpha(fade);
        double shake = fire ? Draw.random(-3, 3) : 0;
        Draw.cover(gc, assets.camera(Room.PISTE_CENTRALE), shake, shake, Draw.W, Draw.H, zoom);
        if (fire) {
            double flick = 0.35 + 0.15 * Math.sin(local * 23) + Draw.random(0, 0.1);
            gc.setFill(Color.rgb(200, 60, 10, flick));
            gc.fillRect(0, 0, Draw.W, Draw.H);
            gc.setFill(new RadialGradient(0, 0, Draw.W / 2, Draw.H + 80, Draw.W * 0.8, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 150, 40, 0.55 * Math.min(1, local / 3))), new Stop(1, Color.TRANSPARENT)));
            gc.fillRect(0, 0, Draw.W, Draw.H);
        } else {
            gc.setFill(Color.rgb(40, 20, 10, 0.45));
            gc.fillRect(0, 0, Draw.W, Draw.H);
        }
        gc.setGlobalAlpha(1.0);
        Draw.shade(gc, 0.25);
    }

    /** Le journal du lendemain, qui s'abat sur l'écran. */
    private void drawHeadline(GraphicsContext gc, double local) {
        double drop = Math.min(1.0, local / 0.25);
        double scale = 1.6 - 0.6 * drop;
        double alpha = Math.min(1.0, (5.0 - local) / 0.8);
        gc.save();
        gc.setGlobalAlpha(Math.max(0, alpha));
        gc.translate(Draw.W / 2, Draw.H / 2);
        gc.rotate(-4);
        gc.scale(scale, scale);
        gc.setFill(Color.web("#cfc4a8"));
        gc.fillRect(-330, -190, 660, 380);
        gc.setFill(Color.rgb(90, 60, 20, 0.15));
        gc.fillRect(-330, -190, 660, 380);
        Color ink = Color.web("#1d1a16");
        Draw.text(gc, "LE COURRIER DE MALVAUX", 0, -150, assets.serif(30), ink, TextAlignment.CENTER);
        gc.setStroke(ink);
        gc.setLineWidth(2);
        gc.strokeLine(-300, -122, 300, -122);
        Draw.text(gc, "1er novembre 1994", 0, -104, assets.ui(14), Color.web("#4a443a"), TextAlignment.CENTER);
        Draw.text(gc, "INCENDIE AU CIRQUE", 0, -40, assets.title(52), Color.web("#5a0a10"), TextAlignment.CENTER);
        Draw.text(gc, "Les clowns de la troupe restent introuvables", 0, 22, assets.ui(20), ink, TextAlignment.CENTER);
        Draw.text(gc, "Le directeur, Henry Malvaux, refuse de parler.", 0, 60, assets.ui(16),
                Color.web("#4a443a"), TextAlignment.CENTER);
        Draw.text(gc, "« Ils sont toujours là », aurait-il murmuré.", 0, 92, assets.ui(16),
                Color.web("#4a443a"), TextAlignment.CENTER);
        gc.restore();
        gc.setGlobalAlpha(1.0);
        if (local < 0.3) {
            gc.setFill(Color.rgb(255, 255, 255, 0.5 * (1 - local / 0.3)));
            gc.fillRect(0, 0, Draw.W, Draw.H);
        }
    }

    /** Le couloir d'aujourd'hui ; au dernier plan, une silhouette apparaît au fond. */
    private void drawCorridor(GraphicsContext gc, double local, double fade, boolean last) {
        double t = shot == 3 ? local : 8.0 + local;
        gc.setGlobalAlpha(fade);
        Draw.cover(gc, assets.camera(Room.COULOIR_GAUCHE), 0, 0, Draw.W, Draw.H, 1.0 + t * 0.02);
        gc.setGlobalAlpha(1.0);
        Draw.shade(gc, last ? 0.45 : 0.3);
        if (last) {
            boolean flicker = Math.random() < 0.15 || (time > SHADOW_AT - 0.6 && time < SHADOW_AT);
            if (flicker) {
                Draw.shade(gc, 0.7);
            }
            if (time > SHADOW_AT && time < SHADOW_AT + 1.4) {
                double grow = Math.min(1, (time - SHADOW_AT) / 1.4);
                Draw.spriteAt(gc, assets.silhouette("bobo", "door", 220 + 260 * grow), Draw.W / 2, Draw.H / 2 + 150 + 120 * grow);
                gc.setFill(Color.rgb(255, 30, 40, 0.9));
                double eyes = 220 + 260 * grow;
                gc.fillOval(Draw.W / 2 - eyes * 0.09, Draw.H / 2 + 150 + 120 * grow - eyes * 0.78, 6, 6);
                gc.fillOval(Draw.W / 2 + eyes * 0.05, Draw.H / 2 + 150 + 120 * grow - eyes * 0.78, 6, 6);
            } else if (time >= SHADOW_AT + 1.4) {
                Draw.clear(gc, Color.BLACK);
            }
        }
    }

    /** Le titre, en lettres d'horreur dont le sang goutte. */
    private void drawTitle(GraphicsContext gc, double local) {
        double alpha = Math.min(1.0, local / 1.5) * Math.min(1.0, (6.5 - local) / 1.0);
        gc.setGlobalAlpha(Math.max(0, alpha));
        gc.setFill(new RadialGradient(0, 0, Draw.W / 2, Draw.H / 2, 520, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(90, 8, 16, 0.5)), new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, 0, Draw.W, Draw.H);
        Draw.text(gc, "M I N U I T   A U", Draw.W / 2, Draw.H / 2 - 120, assets.serif(30), Draw.CREAM, TextAlignment.CENTER);
        Ui.ghostText(gc, "CIRQUE MALVAUX", Draw.W / 2, Draw.H / 2 - 30, assets.title(86), Draw.RED,
                TextAlignment.CENTER, 3);
        double w = Ui.width("CIRQUE MALVAUX", assets.title(86));
        Ui.drips(gc, Draw.W / 2 - w / 2 + 10, Draw.H / 2 + 16, w - 20, 30 + Math.min(40, local * 12), 1994);
        Draw.text(gc, "Cinq nuits. Une régie. Ne laissez rien entrer.", Draw.W / 2, Draw.H / 2 + 110,
                assets.ui(18), Draw.TEXT, TextAlignment.CENTER);
        gc.setGlobalAlpha(1.0);
    }

    private static int shotAt(double time) {
        for (int i = SHOTS.length - 1; i >= 0; i--) {
            if (time >= SHOTS[i].start()) {
                return i;
            }
        }
        return 0;
    }

    private static int typed(Shot s, double time) {
        double local = time - s.start() - 0.8;
        return (int) Math.max(0, Math.min(s.text().length(), local * TYPE_SPEED));
    }

    @Override
    public void onKey(KeyEvent event) {
        KeyCode code = event.getCode();
        if (time > 0.5 && (code == KeyCode.ENTER || code == KeyCode.SPACE || code == KeyCode.ESCAPE)) {
            finish();
        }
    }

    @Override
    public void onClick(double x, double y) {
        if (time > 0.5) {
            finish();
        }
    }
}
