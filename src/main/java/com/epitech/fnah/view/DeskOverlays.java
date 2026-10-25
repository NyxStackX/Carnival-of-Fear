package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.model.Safe;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Fenêtres ouvertes depuis le bureau de la régie : le coffre du directeur
 * (cadran à trois chiffres), le carnet du veilleur (indices et pages
 * retrouvées) et la coupure de journal de 1994. Pendant qu'une fenêtre est
 * ouverte, la nuit continue : les clowns n'attendent pas.
 */
final class DeskOverlays {

    /** Fenêtre ouverte. */
    enum Kind {
        /** Aucune. */
        NONE,
        /** Le cadran du coffre. */
        SAFE,
        /** Le carnet du veilleur. */
        JOURNAL,
        /** La coupure de journal. */
        ARTICLE
    }

    /** Pages du journal de Henry, une par nuit, trouvées dans le coffre. */
    static final String[][] DIARY = {
        {"Page du 2 octobre 1994", "Les clowns réclament leurs gages. Bobo dit qu'ils partiront après "
                + "Halloween. Ils ne partiront pas. Le cirque, c'est eux. Sans eux, il n'y a plus rien."},
        {"Page du 12 octobre 1994", "Mimi s'est cachée dans les loges toute la nuit. Elle passe par le "
                + "couloir droit quand personne ne regarde. J'ai fait poser des portes en fer."},
        {"Page du 20 octobre 1994", "J'ai remonté l'orgue de la fosse. Tant qu'il joue, Pépin reste dans "
                + "sa boîte. S'il s'arrête... on m'a dit de me cacher, de ne plus respirer."},
        {"Page du 30 octobre 1994", "Demain soir, dernière représentation. J'ai fermé les loges de "
                + "l'extérieur. Ils resteront. Pour toujours. J'ai acheté de l'essence."},
        {"Page du 1er novembre 1994", "Je les entends encore. Ils chantent ma mélodie dans le noir. "
                + "Si quelqu'un lit ceci : à 6 heures, ils dorment. Tenez jusque-là. Ne coupez jamais le courant."},
    };
    private static final String[] ARTICLE = {
        "INCENDIE AU CIRQUE MALVAUX",
        "",
        "Dans la nuit du 31 octobre, un incendie a ravagé les loges du Cirque Malvaux",
        "pendant la dernière représentation de la saison. Les spectateurs ont pu fuir.",
        "",
        "Les clowns de la troupe, Bobo, Mimi, les jumeaux Zig et Zag et le petit Pépin,",
        "n'ont jamais été retrouvés. Les portes des loges étaient fermées de l'extérieur.",
        "",
        "Le directeur, Henry Malvaux, a été vu errant sous le chapiteau au petit matin,",
        "fredonnant un air de boîte à musique. Il n'a fait aucune déclaration.",
        "",
        "Le chapiteau est fermé jusqu'à nouvel ordre.",
    };
    private static final String[] ROMAN = {"I", "II", "III"};
    // Fenêtre et cadran du coffre
    private static final double X = 300, Y = 90, WIN_W = 680, WIN_H = 520;
    private static final double PAD_X = X + 380, PAD_Y = Y + 130, KEY_W = 74, KEY_H = 56, KEY_GAP = 10;

    private final Game game;
    private final Assets assets;
    private final TreeSet<Integer> pages = new TreeSet<>();
    private Kind open = Kind.NONE;
    private String message;
    private Color messageColor = Draw.TEXT;
    private double messageTimer;
    private double time;
    private double mouseX;
    private double mouseY;

    DeskOverlays(Game game, Assets assets) {
        this.game = game;
        this.assets = assets;
        game.getEvents().subscribe(GameEvent.SafeDial.class, e -> onDial(e.result()));
    }

    boolean isOpen() {
        return open != Kind.NONE;
    }

    Kind getOpen() {
        return open;
    }

    void open(Kind kind) {
        open = kind;
        message = null;
        game.getOffice().getSafe().clearEntry();
    }

    void close() {
        open = Kind.NONE;
    }

    /**
     * Nombre de pages du journal de Henry déjà retrouvées.
     *
     * @return nombre de pages
     */
    int pageCount() {
        return pages.size();
    }

    void update(double dt) {
        time += dt;
        messageTimer = Math.max(0, messageTimer - dt);
    }

    private void onDial(Safe.Result result) {
        switch (result) {
            case OPENED -> {
                int page = Math.min(game.getCurrentNight(), DIARY.length) - 1;
                pages.add(page);
                say("Le coffre s'ouvre. Batterie de secours branchée : +"
                        + (int) Game.SAFE_ENERGY + " % d'énergie.", Draw.GOLD);
            }
            case WRONG -> say("Mauvais code. Le cliquetis résonne dans les couloirs...", Ui.ALERT);
            default -> {
                // Chiffre noté : rien à afficher.
            }
        }
    }

    private void say(String text, Color color) {
        message = text;
        messageColor = color;
        messageTimer = 4.0;
    }

    // ------------------------------------------------------------------ entrées

    /**
     * Touche pressée pendant qu'une fenêtre est ouverte.
     *
     * @param event touche
     * @return {@code true} si la touche a été utilisée ici
     */
    boolean onKey(KeyEvent event) {
        if (!isOpen()) {
            return false;
        }
        KeyCode code = event.getCode();
        if (code == KeyCode.ESCAPE || code == KeyCode.BACK_SPACE && open != Kind.SAFE
                || code == KeyCode.J && open == Kind.JOURNAL || code == KeyCode.SPACE) {
            close();
            return code != KeyCode.SPACE;
        }
        if (open == Kind.SAFE) {
            if (code == KeyCode.BACK_SPACE) {
                game.getOffice().getSafe().clearEntry();
                return true;
            }
            int digit = NightScreen.cameraNumber(event);
            if (digit < 0 && event.getText() != null && event.getText().equals("à")) {
                digit = 0;
            }
            if (digit < 0 && (code == KeyCode.DIGIT0 || code == KeyCode.NUMPAD0)) {
                digit = 0;
            }
            if (digit >= 0 && digit <= 9) {
                game.dialSafe(digit);
            }
        }
        return true;
    }

    /**
     * Clic pendant qu'une fenêtre est ouverte.
     *
     * @param x abscisse
     * @param y ordonnée
     * @return {@code true} si le clic a été utilisé ici
     */
    boolean onClick(double x, double y) {
        if (!isOpen()) {
            return false;
        }
        if (!Draw.inside(x, y, X, Y, WIN_W, WIN_H) || Draw.inside(x, y, X + WIN_W - 150, Y + WIN_H - 56, 130, 40)) {
            close();
            return true;
        }
        if (open == Kind.SAFE) {
            int key = keyAt(x, y);
            if (key >= 0 && key <= 9) {
                game.dialSafe(key);
            } else if (key == 10) {
                game.getOffice().getSafe().clearEntry();
            }
        }
        return true;
    }

    void onMouseMove(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    /** Touche du cadran sous la souris : 0 à 9, 10 pour effacer, -1 sinon. */
    private int keyAt(double x, double y) {
        int[] layout = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, -1};
        for (int i = 0; i < layout.length; i++) {
            double kx = PAD_X + (i % 3) * (KEY_W + KEY_GAP);
            double ky = PAD_Y + (i / 3) * (KEY_H + KEY_GAP);
            if (layout[i] >= 0 && Draw.inside(x, y, kx, ky, KEY_W, KEY_H)) {
                return layout[i];
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------ dessin

    void render(GraphicsContext gc) {
        if (!isOpen()) {
            return;
        }
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, Draw.W, Draw.H);
        switch (open) {
            case SAFE -> drawSafe(gc);
            case JOURNAL -> drawJournal(gc);
            case ARTICLE -> drawArticle(gc);
            default -> {
                // Rien à dessiner.
            }
        }
        if (open != Kind.ARTICLE) {
            boolean hover = Draw.inside(mouseX, mouseY, X + WIN_W - 150, Y + WIN_H - 56, 130, 40);
            Ui.button(gc, X + WIN_W - 150, Y + WIN_H - 56, 130, 40, "FERMER", assets.ui(15), hover);
            Ui.hints(gc, X + 30, Y + WIN_H - 36, TextAlignment.LEFT, assets.ui(12), "Échap", "Fermer");
        }
        if (messageTimer > 0 && message != null) {
            gc.setGlobalAlpha(Math.min(1, messageTimer));
            Ui.smear(gc, Draw.W / 2, Y + WIN_H + 34, 900, 60, 0.9);
            Draw.text(gc, message, Draw.W / 2, Y + WIN_H + 34, assets.ui(17), messageColor, TextAlignment.CENTER);
            gc.setGlobalAlpha(1.0);
        }
    }

    private void drawSafe(GraphicsContext gc) {
        Safe safe = game.getOffice().getSafe();
        Ui.window(gc, X, Y, WIN_W, WIN_H, "COFFRE DU DIRECTEUR", assets.title(30));
        if (safe.isOpen()) {
            drawOpenSafe(gc);
            return;
        }
        // Les trois cases du code
        Draw.text(gc, "CODE", X + 190, Y + 126, assets.serif(18), Draw.TEXT_DIM, TextAlignment.CENTER);
        for (int i = 0; i < Safe.DIGITS; i++) {
            double bx = X + 70 + i * 84;
            double by = Y + 146;
            Ui.panel(gc, bx, by, 72, 92, i < safe.getTypedCount() ? Draw.GOLD : Ui.EDGE);
            Draw.text(gc, ROMAN[i], bx + 36, by - 0 + 16, assets.serif(13), Draw.TEXT_DIM, TextAlignment.CENTER);
            String shown = i < safe.getTypedCount() ? "*" : "_";
            Draw.text(gc, shown, bx + 36, by + 54, assets.title(44), Draw.CREAM, TextAlignment.CENTER);
        }
        // Indices trouvés sur les caméras
        Draw.text(gc, "CHIFFRES REPÉRÉS", X + 190, Y + 290, assets.serif(18), Draw.GOLD, TextAlignment.CENTER);
        String[] rooms = {"Gradins (CAM 2)", "Loges (CAM 5)", "Ménagerie (CAM 6)"};
        for (int i = 0; i < Safe.DIGITS; i++) {
            boolean known = safe.isDiscovered(i);
            String value = known ? Integer.toString(safe.digit(i)) : "?";
            Draw.text(gc, ROMAN[i] + "  =  " + value, X + 70, Y + 328 + i * 32, assets.ui(18),
                    known ? Draw.CREAM : Draw.TEXT_DIM, TextAlignment.LEFT);
            Draw.text(gc, rooms[i], X + 170, Y + 328 + i * 32, assets.ui(14), Draw.TEXT_DIM, TextAlignment.LEFT);
        }
        Draw.text(gc, "Les chiffres sont griffonnés sur les murs. Regardez bien les caméras.", X + WIN_W / 2,
                Y + 440, assets.ui(14), Draw.TEXT_DIM, TextAlignment.CENTER);

        // Cadran
        String[] labels = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", ""};
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].isEmpty()) {
                continue;
            }
            double kx = PAD_X + (i % 3) * (KEY_W + KEY_GAP);
            double ky = PAD_Y + (i / 3) * (KEY_H + KEY_GAP);
            boolean hover = Draw.inside(mouseX, mouseY, kx, ky, KEY_W, KEY_H);
            Ui.button(gc, kx, ky, KEY_W, KEY_H, labels[i], assets.title(26), hover);
        }
        Draw.text(gc, "Tapez le code au clavier ou à la souris", PAD_X + 116, PAD_Y - 22, assets.ui(13),
                Draw.TEXT_DIM, TextAlignment.CENTER);
    }

    private void drawOpenSafe(GraphicsContext gc) {
        int page = Math.min(game.getCurrentNight(), DIARY.length) - 1;
        Draw.text(gc, "Le coffre est ouvert.", X + WIN_W / 2, Y + 128, assets.serif(24), Draw.CREAM, TextAlignment.CENTER);
        Draw.text(gc, "La batterie de secours a rendu " + (int) Game.SAFE_ENERGY + " % d'énergie.", X + WIN_W / 2,
                Y + 162, assets.ui(16), Draw.GOLD, TextAlignment.CENTER);
        drawPage(gc, DIARY[page], X + 70, Y + 210, WIN_W - 140);
        Draw.text(gc, "Page rangée dans le carnet (touche J).", X + WIN_W / 2, Y + 420, assets.ui(14),
                Draw.TEXT_DIM, TextAlignment.CENTER);
    }

    /** Une page de journal sur papier jauni, écriture à la main. */
    private void drawPage(GraphicsContext gc, String[] page, double x, double y, double w) {
        List<String> lines = NightScreen.wrap(page[1], (int) (w / 9.2));
        double h = 60 + lines.size() * 26;
        gc.save();
        gc.translate(x + w / 2, y + h / 2);
        gc.rotate(-1.2);
        gc.setFill(Color.web("#cdbf9c"));
        gc.fillRect(-w / 2, -h / 2, w, h);
        gc.setFill(Color.rgb(90, 50, 20, 0.18));
        gc.fillRect(-w / 2, -h / 2, w, h);
        Draw.text(gc, page[0], -w / 2 + 20, -h / 2 + 24, assets.serif(17), Color.web("#4a1e14"), TextAlignment.LEFT);
        for (int i = 0; i < lines.size(); i++) {
            Draw.text(gc, lines.get(i), -w / 2 + 20, -h / 2 + 54 + i * 26, assets.ui(15), Color.web("#2a2018"),
                    TextAlignment.LEFT);
        }
        gc.restore();
    }

    private void drawJournal(GraphicsContext gc) {
        Ui.window(gc, X, Y, WIN_W, WIN_H, "CARNET DU VEILLEUR", assets.title(30));
        Safe safe = game.getOffice().getSafe();
        double y = Y + 122;
        Draw.text(gc, "Objectif : tenir jusqu'à 6 h. Ne laissez rien entrer.", X + 50, y, assets.ui(16),
                Draw.CREAM, TextAlignment.LEFT);
        StringBuilder code = new StringBuilder("Code du coffre (sous le bureau) :  ");
        for (int i = 0; i < Safe.DIGITS; i++) {
            code.append(ROMAN[i]).append(" = ").append(safe.isDiscovered(i) ? safe.digit(i) : "?").append("   ");
        }
        Draw.text(gc, code.toString(), X + 50, y + 32, assets.ui(16),
                safe.isOpen() ? Draw.TEXT_DIM : Draw.GOLD, TextAlignment.LEFT);
        String[] notes = {
            "Le bruit les attire : portes, manivelle, levier, mauvais code.",
            "Orgue arrêté : remontez-le (CAM 8) ou cachez-vous (S) sans bruit.",
            "Fusible grillé : maintenez F sur le disjoncteur, en haut à gauche.",
        };
        for (int i = 0; i < notes.length; i++) {
            Draw.text(gc, "- " + notes[i], X + 50, y + 74 + i * 26, assets.ui(14), Draw.TEXT, TextAlignment.LEFT);
        }
        if (pages.isEmpty()) {
            Draw.text(gc, "Aucune page du journal de Henry retrouvée pour l'instant.", X + WIN_W / 2, y + 220,
                    assets.ui(15), Draw.TEXT_DIM, TextAlignment.CENTER);
        } else {
            Draw.text(gc, "Pages du journal de Henry retrouvées : " + pages.size() + " / " + DIARY.length,
                    X + WIN_W / 2, y + 166, assets.ui(14), Draw.GOLD, TextAlignment.CENTER);
            drawPage(gc, DIARY[pages.last()], X + 60, y + 190, WIN_W - 120);
        }
    }

    private void drawArticle(GraphicsContext gc) {
        gc.save();
        gc.translate(Draw.W / 2, Draw.H / 2 - 10);
        gc.rotate(-1.5);
        double w = 760;
        double h = 470;
        gc.setFill(Color.web("#cfc4a8"));
        gc.fillRect(-w / 2, -h / 2, w, h);
        gc.setFill(Color.rgb(90, 60, 20, 0.15));
        gc.fillRect(-w / 2, -h / 2, w, h);
        Color ink = Color.web("#1d1a16");
        Draw.text(gc, "LE COURRIER DE MALVAUX  -  1er novembre 1994", 0, -h / 2 + 30, assets.serif(18), ink,
                TextAlignment.CENTER);
        gc.setStroke(ink);
        gc.setLineWidth(1.5);
        gc.strokeLine(-w / 2 + 30, -h / 2 + 50, w / 2 - 30, -h / 2 + 50);
        Draw.text(gc, ARTICLE[0], 0, -h / 2 + 92, assets.title(36), Color.web("#5a0a10"), TextAlignment.CENTER);
        for (int i = 1; i < ARTICLE.length; i++) {
            Draw.text(gc, ARTICLE[i], 0, -h / 2 + 120 + i * 24, assets.ui(15), ink, TextAlignment.CENTER);
        }
        gc.restore();
        Ui.hints(gc, Draw.W / 2, Draw.H - 40, TextAlignment.CENTER, assets.ui(13), "Échap", "Reposer la coupure");
    }

    /**
     * Nombre de chiffres du code déjà repérés.
     *
     * @return entre 0 et 3
     */
    int cluesFound() {
        int count = 0;
        for (int i = 0; i < Safe.DIGITS; i++) {
            if (game.getOffice().getSafe().isDiscovered(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Liste des pages trouvées (pour les tests visuels).
     *
     * @return rangs des pages
     */
    List<Integer> pages() {
        return new ArrayList<>(pages);
    }
}
