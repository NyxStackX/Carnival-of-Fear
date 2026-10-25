package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.model.Room;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Vraie fin, après la nuit 5 : le journal du lendemain annonce la disparition
 * du veilleur, puis le générique présente toute la troupe.
 */
final class EndingScreen implements Screen {

    private static final double NEWSPAPER_MIN = 2.0;
    private static final double NEWSPAPER_AUTO = 14.0;
    private static final double CREDITS_SPEED = 55;

    /** Les clowns du générique : dossier d'image, nom, rôle. */
    private static final String[][] TROUPE = {
        {"bobo", "BOBO", "Le clown triste. Il frappe à la porte gauche."},
        {"mimi", "MIMI", "La contorsionniste. Elle se faufile par le couloir droit."},
        {"zig_et_zag", "ZIG ET ZAG", "Les jumeaux farceurs. Ils brouillent les caméras."},
        {"pepin", "PÉPIN", "Le diable à ressort. Il dort tant que l'orgue joue."},
        {"henry", "HENRY", "Le directeur. Il revient quand la lumière s'éteint."},
    };

    private static final String[] CREDITS_END = {
        "",
        "UN JEU RÉALISÉ POUR LE PROJET 2D GAME",
        "Epitech Digital School - Bordeaux",
        "",
        "CODE, DESSINS ET HISTOIRE",
        "L'équipe du projet",
        "",
        "MUSIQUES",
        "Retro Electronics Prologue - Serenade",
        "",
        "EFFETS SONORES",
        "Créés par synthèse (tools/generate_sounds.py)",
        "",
        "POLICES",
        "Eater - IM Fell English - Rock Salt - Special Elite",
        "",
        "",
        "En souvenir de Marcel, veilleur de nuit.",
        "Et de vous.",
    };

    private final Game game;
    private final Assets assets;
    private Image newspaper;
    private double time;
    private boolean credits;
    private double creditsTime;
    private boolean creditsDone;

    EndingScreen(Game game, Assets assets) {
        this.game = game;
        this.assets = assets;
    }

    @Override
    public void onEnter() {
        time = 0;
        credits = false;
        creditsTime = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
        if (!credits && time > NEWSPAPER_AUTO) {
            startCredits();
        }
        if (credits) {
            creditsTime += dt;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        Draw.clear(gc, Color.BLACK);
        if (!credits) {
            drawNewspaper(gc);
        } else {
            drawCredits(gc);
        }
        Draw.grain(gc, 300, 0.12);
        Draw.overlay(gc, 0, 0.12, 0.9);
    }

    private void drawNewspaper(GraphicsContext gc) {
        if (newspaper == null) {
            newspaper = Draw.render(900, 640, this::paintNewspaper);
        }
        double appear = Math.min(1.0, time / 1.5);
        gc.save();
        gc.setGlobalAlpha(appear);
        gc.translate(Draw.W / 2, Draw.H / 2 + 20 * (1 - appear));
        gc.rotate(-2 + 2 * appear);
        gc.drawImage(newspaper, -450, -320);
        gc.restore();
        if (time > NEWSPAPER_MIN) {
            Ui.hints(gc, Draw.W - 40, Draw.H - 26, TextAlignment.RIGHT, assets.ui(13), "Entrée", "Continuer");
        }
    }

    private void paintNewspaper(GraphicsContext g) {
        Color ink = Color.web("#1d1a16");
        Color faded = Color.web("#4a443a");
        g.setFill(Color.web("#d9d0b8"));
        g.fillRect(0, 0, 900, 640);
        g.setFill(Color.rgb(120, 90, 40, 0.12));
        g.fillRect(0, 0, 900, 640);
        Draw.text(g, "LE COURRIER DE MALVAUX", 450, 52, assets.title(46), ink, TextAlignment.CENTER);
        g.setStroke(ink);
        g.setLineWidth(2);
        g.strokeLine(30, 86, 870, 86);
        Draw.text(g, "Édition du matin", 40, 100, assets.ui(13), faded, TextAlignment.LEFT);
        Draw.text(g, "Prix : 1 franc", 860, 100, assets.ui(13), faded, TextAlignment.RIGHT);
        g.strokeLine(30, 112, 870, 112);
        Draw.text(g, "LE VEILLEUR A DISPARU", 450, 160, assets.title(56), ink, TextAlignment.CENTER);
        Draw.text(g, "Le gardien de nuit du Cirque Malvaux reste introuvable après sa cinquième nuit",
                450, 206, assets.ui(17), faded, TextAlignment.CENTER);

        // Photo : la piste, en sépia, avec la troupe au complet... et un de plus
        double px = 40;
        double py = 236;
        double pw = 430;
        double ph = 330;
        Draw.cover(g, assets.camera(Room.PISTE_CENTRALE), px, py, pw, ph, 1.0);
        g.setFill(Color.rgb(112, 80, 40, 0.45));
        g.fillRect(px, py, pw, ph);
        String[] shadows = {"bobo", "mimi", "pepin", "zig_et_zag"};
        for (int i = 0; i < shadows.length; i++) {
            g.setGlobalAlpha(0.55);
            Draw.spriteAt(g, assets.silhouette(shadows[i], "idle_1", 150), px + 60 + i * 90, py + ph - 10);
        }
        g.setGlobalAlpha(0.35);
        Draw.spriteAt(g, assets.silhouette("bobo", "idle_2", 120), px + pw - 40, py + ph - 20);
        g.setGlobalAlpha(1.0);
        g.setStroke(ink);
        g.strokeRect(px, py, pw, ph);
        Draw.text(g, "La piste centrale, ce matin. Combien en comptez-vous ?", px, py + ph + 18, assets.ui(12),
                faded, TextAlignment.LEFT);

        String[] article = {
            "Après cinq nuits passées à",
            "surveiller le chapiteau",
            "abandonné, le veilleur engagé",
            "par la famille Malvaux n'a pas",
            "reparu ce matin.",
            "",
            "La régie a été retrouvée intacte,",
            "les portes ouvertes, la tablette",
            "encore allumée sur la caméra 1.",
            "",
            "Comme en 1994, aucune trace.",
            "Seul détail troublant selon la",
            "police : sur l'affiche de la",
            "dernière représentation, un",
            "nouveau clown semble avoir été",
            "ajouté à la troupe.",
        };
        for (int i = 0; i < article.length; i++) {
            Draw.text(g, article[i], 500, 246 + i * 21, assets.ui(15), ink, TextAlignment.LEFT);
        }
        g.setFill(Color.rgb(60, 40, 20, 0.08));
        for (int i = 0; i < 60; i++) {
            double r = Draw.random(10, 60);
            g.fillOval(Draw.random(0, 900), Draw.random(0, 640), r, r);
        }
    }

    private void drawCredits(GraphicsContext gc) {
        double scroll = creditsTime * CREDITS_SPEED;
        double cx = Draw.W / 2;
        double y = Draw.H + 60 - scroll;
        Draw.shadowText(gc, "MINUIT AU", cx, y, assets.title(34), Draw.CREAM, TextAlignment.CENTER);
        Draw.shadowText(gc, "CIRQUE MALVAUX", cx, y + 70, assets.title(72), Draw.RED, TextAlignment.CENTER);
        y += 200;
        Draw.text(gc, "LA TROUPE", cx, y, assets.title(36), Draw.GOLD, TextAlignment.CENTER);
        y += 60;
        for (int i = 0; i < TROUPE.length; i++) {
            boolean left = i % 2 == 0;
            double spriteX = left ? cx - 260 : cx + 260;
            double textX = left ? cx - 120 : cx + 120;
            TextAlignment align = left ? TextAlignment.LEFT : TextAlignment.RIGHT;
            String frame = new String[]{"idle_1", "idle_2", "idle_3"}[(int) (time * 1.2 + i) % 3];
            Draw.spriteAt(gc, assets.sprite(TROUPE[i][0], frame, 260), spriteX, y + 250);
            Draw.shadowText(gc, TROUPE[i][1], textX, y + 110, assets.title(40), Draw.CREAM, align);
            Draw.text(gc, TROUPE[i][2], textX, y + 160, assets.ui(18), Draw.TEXT, align);
            y += 290;
        }
        for (String line : CREDITS_END) {
            boolean heading = !line.isEmpty() && line.equals(line.toUpperCase()) && line.length() > 3;
            Draw.text(gc, line, cx, y, heading ? assets.title(26) : assets.ui(20),
                    heading ? Draw.GOLD : Draw.TEXT, TextAlignment.CENTER);
            y += heading ? 40 : 34;
        }
        double endY = y + 140;
        Draw.shadowText(gc, "FIN", cx, Math.max(Draw.H / 2, endY), assets.title(110), Draw.RED, TextAlignment.CENTER);
        if (endY <= Draw.H / 2) {
            Ui.hints(gc, cx, Draw.H / 2 + 110, TextAlignment.CENTER, assets.ui(14), "Entrée", "Retour au menu");
        } else {
            Ui.hints(gc, Draw.W - 40, Draw.H - 26, TextAlignment.RIGHT, assets.ui(13), "Entrée", "Accélérer",
                    "Échap", "Menu");
        }
        creditsDone = endY <= Draw.H / 2;
    }

    private void startCredits() {
        credits = true;
        creditsTime = 0;
        creditsDone = false;
    }

    @Override
    public void onKey(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.ESCAPE) {
            game.returnToMenu();
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            advance();
        }
    }

    @Override
    public void onClick(double x, double y) {
        advance();
    }

    private void advance() {
        if (!credits) {
            if (time > NEWSPAPER_MIN) {
                startCredits();
            }
        } else if (creditsDone) {
            game.returnToMenu();
        } else {
            creditsTime += 4;
        }
    }
}
