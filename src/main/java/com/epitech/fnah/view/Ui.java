package com.epitech.fnah.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Composants d'interface communs à tous les écrans, pour un style unique de
 * cirque abandonné : panneaux sombres à filet d'or vieilli, boutons, touches
 * du clavier dessinées, ampoules de chapiteau, étoiles, jauges, bandeaux
 * d'alerte, et les effets d'horreur (texte dédoublé, coulures de sang,
 * pellicule rayée, défilement de cassette).
 *
 * <p>Tout est dessiné au pinceau JavaFX, sans image externe.</p>
 */
final class Ui {

    /** Fond des panneaux. */
    static final Color PANEL = Color.rgb(10, 6, 7, 0.88);
    /** Filet doré des panneaux. */
    static final Color EDGE = Color.rgb(176, 141, 69, 0.45);
    /** Vert de la jauge pleine. */
    static final Color OK = Color.web("#8fc46f");
    /** Rouge d'alerte. */
    static final Color ALERT = Color.web("#ff4d5e");

    private static final Map<String, Double> WIDTHS = new HashMap<>();
    private static final Text MEASURE = new Text();
    private static Image bulbGlow;
    private static final Map<String, Image> CACHE = new HashMap<>();

    private Ui() {
    }

    /**
     * Largeur d'un texte dans une police (mise en cache).
     *
     * @param value texte
     * @param font  police
     * @return largeur en pixels
     */
    static double width(String value, Font font) {
        return WIDTHS.computeIfAbsent(font.getFamily() + font.getSize() + "|" + value, k -> {
            MEASURE.setText(value);
            MEASURE.setFont(font);
            return MEASURE.getLayoutBounds().getWidth();
        });
    }

    /**
     * Panneau sombre aux coins arrondis, filet doré et liseré intérieur.
     *
     * @param gc contexte graphique
     * @param x  gauche
     * @param y  haut
     * @param w  largeur
     * @param h  hauteur
     */
    static void panel(GraphicsContext gc, double x, double y, double w, double h) {
        panel(gc, x, y, w, h, EDGE);
    }

    /**
     * Panneau sombre avec une couleur de filet donnée.
     *
     * @param gc   contexte graphique
     * @param x    gauche
     * @param y    haut
     * @param w    largeur
     * @param h    hauteur
     * @param edge couleur du filet
     */
    static void panel(GraphicsContext gc, double x, double y, double w, double h, Color edge) {
        gc.setFill(PANEL);
        gc.fillRoundRect(x, y, w, h, 14, 14);
        gc.setFill(new LinearGradient(0, y, 0, y + h, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 255, 0.05)), new Stop(0.4, Color.TRANSPARENT)));
        gc.fillRoundRect(x, y, w, h, 14, 14);
        gc.setStroke(edge);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x + 0.5, y + 0.5, w - 1, h - 1, 14, 14);
        gc.setStroke(Color.rgb(255, 255, 255, 0.05));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + 4, y + 4, w - 8, h - 8, 10, 10);
    }

    /**
     * Grand panneau de fenêtre (pause, aide...) avec un titre et deux ornements.
     *
     * @param gc      contexte graphique
     * @param x       gauche
     * @param y       haut
     * @param w       largeur
     * @param h       hauteur
     * @param heading titre
     * @param font    police du titre
     */
    static void window(GraphicsContext gc, double x, double y, double w, double h, String heading, Font font) {
        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(x + 8, y + 12, w, h, 18, 18);
        gc.setFill(Color.rgb(20, 12, 13, 0.97));
        gc.fillRoundRect(x, y, w, h, 18, 18);
        gc.setFill(new LinearGradient(0, y, 0, y + 90, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(120, 20, 30, 0.35)), new Stop(1, Color.TRANSPARENT)));
        gc.fillRoundRect(x, y, w, 90, 18, 18);
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 1, y + 1, w - 2, h - 2, 18, 18);
        gc.setStroke(Color.rgb(201, 162, 74, 0.35));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + 8, y + 8, w - 16, h - 16, 12, 12);
        double cx = x + w / 2;
        Draw.shadowText(gc, heading, cx, y + 50, font, Draw.CREAM, TextAlignment.CENTER);
        double half = Ui.width(heading, font) / 2 + 24;
        ornament(gc, cx - half, y + 52, -1);
        ornament(gc, cx + half, y + 52, 1);
    }

    /** Petit ornement doré (losange et trait) de part et d'autre d'un titre. */
    private static void ornament(GraphicsContext gc, double x, double y, int dir) {
        gc.setStroke(Draw.GOLD);
        gc.setLineWidth(1.5);
        gc.strokeLine(x, y, x + dir * 60, y);
        diamond(gc, x + dir * 66, y, 4, Draw.GOLD);
    }

    /**
     * Petit losange plein.
     *
     * @param gc    contexte graphique
     * @param cx    centre x
     * @param cy    centre y
     * @param r     demi-diagonale
     * @param color couleur
     */
    static void diamond(GraphicsContext gc, double cx, double cy, double r, Color color) {
        gc.setFill(color);
        gc.fillPolygon(new double[]{cx - r, cx, cx + r, cx}, new double[]{cy, cy - r, cy, cy + r}, 4);
    }

    /**
     * Bouton rectangulaire (pause, menus de fenêtre), avec survol.
     *
     * @param gc      contexte graphique
     * @param x       gauche
     * @param y       haut
     * @param w       largeur
     * @param h       hauteur
     * @param label   texte
     * @param font    police
     * @param hovered {@code true} si la souris est dessus
     */
    static void button(GraphicsContext gc, double x, double y, double w, double h, String label, Font font,
                       boolean hovered) {
        gc.setFill(hovered
                ? new LinearGradient(0, y, 0, y + h, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#8a1a26")), new Stop(1, Color.web("#5a0f18")))
                : new LinearGradient(0, y, 0, y + h, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#2c2224")), new Stop(1, Color.web("#1a1314"))));
        gc.fillRoundRect(x, y, w, h, 10, 10);
        gc.setStroke(hovered ? Draw.GOLD : Color.rgb(201, 162, 74, 0.35));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x + 0.5, y + 0.5, w - 1, h - 1, 10, 10);
        Draw.text(gc, label, x + w / 2, y + h / 2, font, hovered ? Draw.CREAM : Draw.TEXT, TextAlignment.CENTER);
    }

    /**
     * Touche de clavier dessinée (ex. « Espace »), alignée à gauche à partir de {@code x}.
     *
     * @param gc   contexte graphique
     * @param key  texte de la touche
     * @param x    gauche
     * @param cy   centre vertical
     * @param font police
     * @return largeur occupée
     */
    static double keycap(GraphicsContext gc, String key, double x, double cy, Font font) {
        double w = Math.max(font.getSize() * 1.6, width(key, font) + font.getSize() * 0.9);
        double h = font.getSize() * 1.55;
        double y = cy - h / 2;
        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(x, y + 2, w, h, 6, 6);
        gc.setFill(new LinearGradient(0, y, 0, y + h, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3b3335")), new Stop(1, Color.web("#241e1f"))));
        gc.fillRoundRect(x, y, w, h, 6, 6);
        gc.setStroke(Color.rgb(232, 224, 208, 0.35));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + 0.5, y + 0.5, w - 1, h - 1, 6, 6);
        Draw.text(gc, key, x + w / 2, cy, font, Draw.CREAM, TextAlignment.CENTER);
        return w;
    }

    /**
     * Ligne d'aide « touche + action », par exemple {@code "Espace", "Caméras", "P", "Pause"}.
     *
     * @param gc    contexte graphique
     * @param x     position de référence
     * @param cy    centre vertical
     * @param align alignement par rapport à {@code x}
     * @param font  police
     * @param pairs touche, action, touche, action...
     */
    static void hints(GraphicsContext gc, double x, double cy, TextAlignment align, Font font, String... pairs) {
        double gap = font.getSize() * 1.4;
        double total = 0;
        for (int i = 0; i < pairs.length; i += 2) {
            total += Math.max(font.getSize() * 1.6, width(pairs[i], font) + font.getSize() * 0.9)
                    + 6 + width(pairs[i + 1], font) + (i + 2 < pairs.length ? gap : 0);
        }
        double cursor = switch (align) {
            case CENTER -> x - total / 2;
            case RIGHT -> x - total;
            default -> x;
        };
        for (int i = 0; i < pairs.length; i += 2) {
            cursor += keycap(gc, pairs[i], cursor, cy, font) + 6;
            Draw.text(gc, pairs[i + 1], cursor, cy, font, Draw.TEXT_DIM, TextAlignment.LEFT);
            cursor += width(pairs[i + 1], font) + gap;
        }
    }

    /**
     * Ampoules de chapiteau tout autour d'un rectangle, avec une chenille lumineuse.
     *
     * @param gc      contexte graphique
     * @param x       gauche
     * @param y       haut
     * @param w       largeur
     * @param h       hauteur
     * @param spacing écart entre deux ampoules
     * @param time    temps, pour l'animation (négatif : ampoules éteintes)
     */
    static void bulbs(GraphicsContext gc, double x, double y, double w, double h, double spacing, double time) {
        int along = (int) (w / spacing);
        int down = (int) (h / spacing);
        int index = 0;
        int phase = time < 0 ? -1 : (int) (time * 6) % 3;
        boolean sputter = Math.random() < 0.5;
        for (int i = 0; i <= along; i++) {
            chaser(gc, x + i * w / along, y, index++, phase, sputter);
        }
        for (int i = 1; i <= down; i++) {
            chaser(gc, x + w, y + i * h / down, index++, phase, sputter);
        }
        for (int i = along - 1; i >= 0; i--) {
            chaser(gc, x + i * w / along, y + h, index++, phase, sputter);
        }
        for (int i = down - 1; i >= 1; i--) {
            chaser(gc, x, y + i * h / down, index++, phase, sputter);
        }
    }

    /** Ampoule de la chenille : certaines sont grillées pour de bon, une grésille. */
    private static void chaser(GraphicsContext gc, double cx, double cy, int index, int phase, boolean sputter) {
        if (index * 7 % 11 == 3 || index * 5 % 13 == 1) {
            gc.setFill(Color.web("#2a221c"));
            gc.fillOval(cx - 3.5, cy - 3.5, 7, 7);
            return;
        }
        boolean lit = index % 17 == 9 ? sputter : index % 3 == phase;
        bulb(gc, cx, cy, lit);
    }

    /**
     * Une ampoule ronde, allumée ou à demi éteinte.
     *
     * @param gc  contexte graphique
     * @param cx  centre x
     * @param cy  centre y
     * @param lit {@code true} pour l'ampoule la plus brillante
     */
    static void bulb(GraphicsContext gc, double cx, double cy, boolean lit) {
        if (lit) {
            if (bulbGlow == null) {
                bulbGlow = Draw.render(24, 24, g -> {
                    g.setFill(new RadialGradient(0, 0, 12, 12, 12, false, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.rgb(255, 214, 140, 0.55)), new Stop(1, Color.TRANSPARENT)));
                    g.fillOval(0, 0, 24, 24);
                });
            }
            gc.drawImage(bulbGlow, Math.round(cx - 12), Math.round(cy - 12));
        }
        gc.setFill(lit ? Color.web("#fff1c8") : Color.web("#b88a3c"));
        gc.fillOval(cx - 3.5, cy - 3.5, 7, 7);
    }

    /**
     * Étoile à cinq branches, pleine si obtenue.
     *
     * @param gc  contexte graphique
     * @param cx  centre x
     * @param cy  centre y
     * @param r   rayon
     * @param lit {@code true} si l'étoile est obtenue
     */
    static void star(GraphicsContext gc, double cx, double cy, double r, boolean lit) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int k = 0; k < 10; k++) {
            double radius = k % 2 == 0 ? r : r * 0.45;
            double angle = -Math.PI / 2 + k * Math.PI / 5;
            xs[k] = cx + Math.cos(angle) * radius;
            ys[k] = cy + Math.sin(angle) * radius;
        }
        if (lit) {
            gc.setFill(new RadialGradient(0, 0, cx, cy, r * 1.8, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 200, 90, 0.35)), new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(cx - r * 1.8, cy - r * 1.8, r * 3.6, r * 3.6);
        }
        gc.setFill(lit ? Draw.GOLD : Color.rgb(255, 255, 255, 0.06));
        gc.fillPolygon(xs, ys, 10);
        gc.setStroke(lit ? Color.web("#f3dfa6") : Draw.TEXT_DIM);
        gc.setLineWidth(1.2);
        gc.strokePolygon(xs, ys, 10);
    }

    /**
     * Jauge horizontale segmentée.
     *
     * @param gc       contexte graphique
     * @param x        gauche
     * @param y        haut
     * @param w        largeur totale
     * @param h        hauteur
     * @param segments nombre de segments
     * @param ratio    remplissage entre 0 et 1
     * @param color    couleur des segments pleins
     */
    static void gauge(GraphicsContext gc, double x, double y, double w, double h, int segments, double ratio,
                      Color color) {
        double gap = 3;
        double sw = (w - gap * (segments - 1)) / segments;
        double filled = ratio * segments;
        for (int i = 0; i < segments; i++) {
            double sx = x + i * (sw + gap);
            gc.setFill(Color.rgb(255, 255, 255, 0.07));
            gc.fillRoundRect(sx, y, sw, h, 3, 3);
            double part = Math.max(0, Math.min(1, filled - i));
            if (part > 0) {
                gc.setFill(color);
                gc.fillRoundRect(sx, y, sw * part, h, 3, 3);
            }
        }
    }

    /**
     * Couleur d'une jauge selon son niveau : vert, ambre puis rouge.
     *
     * @param ratio niveau entre 0 et 1
     * @return couleur adaptée
     */
    static Color levelColor(double ratio) {
        return ratio > 0.5 ? OK : ratio > 0.25 ? Draw.AMBER : ALERT;
    }

    /**
     * Bandeau d'alerte rouge clignotant, centré.
     *
     * @param gc   contexte graphique
     * @param text message
     * @param cx   centre x
     * @param cy   centre y
     * @param font police
     * @param time temps, pour le clignotement
     */
    static void alert(GraphicsContext gc, String text, double cx, double cy, Font font, double time) {
        double w = width(text, font) + 70;
        double h = font.getSize() * 2.1;
        double pulse = 0.5 + 0.5 * Math.sin(time * 8);
        gc.setFill(Color.rgb(40, 4, 8, 0.9));
        gc.fillRoundRect(cx - w / 2, cy - h / 2, w, h, 10, 10);
        gc.setStroke(Color.rgb(255, 77, 94, 0.5 + 0.5 * pulse));
        gc.setLineWidth(2);
        gc.strokeRoundRect(cx - w / 2, cy - h / 2, w, h, 10, 10);
        warningIcon(gc, cx - w / 2 + 24, cy, font.getSize() * 0.7, Color.rgb(255, 77, 94, 0.6 + 0.4 * pulse));
        Draw.text(gc, text, cx + 12, cy, font, Draw.CREAM, TextAlignment.CENTER);
    }

    /**
     * Triangle « attention ».
     *
     * @param gc    contexte graphique
     * @param cx    centre x
     * @param cy    centre y
     * @param r     taille
     * @param color couleur
     */
    static void warningIcon(GraphicsContext gc, double cx, double cy, double r, Color color) {
        gc.setFill(color);
        gc.fillPolygon(new double[]{cx - r, cx, cx + r}, new double[]{cy + r * 0.85, cy - r, cy + r * 0.85}, 3);
        gc.setFill(Color.rgb(30, 4, 8));
        gc.fillRect(cx - 1.2, cy - r * 0.35, 2.4, r * 0.65);
        gc.fillRect(cx - 1.2, cy + r * 0.45, 2.4, 2.4);
    }

    /**
     * Pile dessinée, remplie selon le niveau.
     *
     * @param gc    contexte graphique
     * @param x     gauche
     * @param y     haut
     * @param w     largeur (sans la borne)
     * @param h     hauteur
     * @param ratio niveau entre 0 et 1
     * @param color couleur de remplissage
     */
    static void battery(GraphicsContext gc, double x, double y, double w, double h, double ratio, Color color) {
        gc.setStroke(Draw.TEXT);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, w, h, 5, 5);
        gc.setFill(Draw.TEXT);
        gc.fillRoundRect(x + w + 1, y + h * 0.3, 4, h * 0.4, 2, 2);
        gc.setFill(color);
        gc.fillRoundRect(x + 3, y + 3, Math.max(0, (w - 6) * ratio), h - 6, 3, 3);
    }

    /**
     * Texte dédoublé comme une image VHS abîmée : une ombre rouge et une ombre
     * cyan légèrement décalées, puis le texte. Les décalages tremblent un peu.
     *
     * @param gc     contexte graphique
     * @param value  texte
     * @param x      position
     * @param y      position (centre vertical)
     * @param font   police
     * @param color  couleur du texte
     * @param align  alignement
     * @param amount force du dédoublement en pixels
     */
    static void ghostText(GraphicsContext gc, String value, double x, double y, Font font, Color color,
                          TextAlignment align, double amount) {
        double jx = amount > 0 && Math.random() < 0.06 ? Draw.random(-amount * 2, amount * 2) : 0;
        Draw.text(gc, value, x + 3, y + 4, font, Color.rgb(0, 0, 0, 0.85), align);
        Draw.text(gc, value, x - amount + jx, y, font, Color.rgb(0, 200, 220, 0.22), align);
        Draw.text(gc, value, x + amount + jx, y, font, Color.rgb(255, 0, 30, 0.45), align);
        Draw.text(gc, value, x + jx * 0.5, y, font, color, align);
    }

    /**
     * Ombre douce derrière un texte, pour qu'il reste lisible sans encadré.
     *
     * @param gc    contexte graphique
     * @param cx    centre x
     * @param cy    centre y
     * @param w     largeur
     * @param h     hauteur
     * @param alpha opacité au centre
     */
    static void smear(GraphicsContext gc, double cx, double cy, double w, double h, double alpha) {
        Image image = CACHE.computeIfAbsent("smear", k -> Draw.render(256, 64, g -> {
            g.setFill(new RadialGradient(0, 0, 128, 32, 128, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(0, 0, 0, 1)), new Stop(0.6, Color.rgb(0, 0, 0, 0.6)),
                    new Stop(1, Color.TRANSPARENT)));
            g.save();
            g.scale(1, 0.25);
            g.fillOval(0, 0, 256, 256);
            g.restore();
        }));
        gc.save();
        gc.setGlobalAlpha(gc.getGlobalAlpha() * alpha);
        gc.drawImage(image, cx - w / 2, cy - h / 2, w, h);
        gc.restore();
    }

    /**
     * Coulures de sang qui partent d'un bord (calculées une fois par taille).
     *
     * @param gc     contexte graphique
     * @param x      gauche
     * @param y      bord d'où coule le sang
     * @param w      largeur couverte
     * @param maxLen longueur maximale d'une coulure
     * @param seed   graine, pour que les coulures restent les mêmes
     */
    static void drips(GraphicsContext gc, double x, double y, double w, double maxLen, long seed) {
        Image image = CACHE.computeIfAbsent("drips" + (int) w + "/" + (int) maxLen + "/" + seed,
                k -> Draw.render(w, maxLen + 10, g -> {
                    Random random = new Random(seed);
                    g.setFill(Color.web("#6e0912"));
                    g.fillRect(0, 0, w, 3);
                    for (double dx = 4; dx < w - 4; dx += 7 + random.nextDouble() * 22) {
                        double len = 4 + Math.pow(random.nextDouble(), 2.2) * maxLen;
                        double width = 2 + random.nextDouble() * 4;
                        g.setFill(new LinearGradient(0, 0, 0, len, false, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#7a0a14")), new Stop(1, Color.web("#4a0309"))));
                        g.fillRoundRect(dx, 0, width, len, width, width);
                        g.fillOval(dx - 1, len - width * 0.4, width + 2, width + 2);
                    }
                }));
        gc.drawImage(image, Math.round(x), Math.round(y));
    }

    /**
     * Griffure rouge irrégulière sous un texte (sélection).
     *
     * @param gc   contexte graphique
     * @param x    gauche
     * @param y    hauteur de la griffure
     * @param w    longueur
     * @param seed graine, pour que la griffure ne change pas à chaque image
     */
    static void scratch(GraphicsContext gc, double x, double y, double w, long seed) {
        Random random = new Random(seed);
        gc.setStroke(Color.rgb(176, 18, 31, 0.9));
        for (int line = 0; line < 2; line++) {
            gc.setLineWidth(line == 0 ? 2.5 : 1.2);
            double px = x;
            double py = y + line * 4;
            for (double cx = x + 10; cx <= x + w; cx += 10) {
                double ny = y + line * 4 + random.nextDouble() * 3 - 1.5;
                gc.strokeLine(px, py, cx, ny);
                px = cx;
                py = ny;
            }
        }
    }

    /**
     * Rayures verticales et poussières d'une vieille pellicule.
     *
     * @param gc contexte graphique
     */
    static void filmDamage(GraphicsContext gc) {
        if (Math.random() < 0.5) {
            int lines = 1 + (int) (Math.random() * 2);
            for (int i = 0; i < lines; i++) {
                double x = Draw.random(0, Draw.W);
                gc.setStroke(Color.rgb(230, 225, 210, Draw.random(0.05, 0.18)));
                gc.setLineWidth(Draw.random(0.6, 1.6));
                gc.strokeLine(x, 0, x + Draw.random(-6, 6), Draw.H);
            }
        }
        if (Math.random() < 0.25) {
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            double r = Draw.random(2, 6);
            gc.fillOval(Draw.random(0, Draw.W), Draw.random(0, Draw.H), r, r * Draw.random(0.4, 1.4));
        }
    }

    /**
     * Bande de défilement d'une cassette VHS qui glisse lentement vers le bas.
     *
     * @param gc   contexte graphique
     * @param time temps, pour le défilement
     */
    static void vhsTracking(GraphicsContext gc, double time) {
        double y = (time * 70) % (Draw.H + 160) - 80;
        gc.setFill(new LinearGradient(0, y - 30, 0, y + 30, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT), new Stop(0.5, Color.rgb(220, 220, 220, 0.07)),
                new Stop(1, Color.TRANSPARENT)));
        gc.fillRect(0, y - 30, Draw.W, 60);
        Draw.grain(gc, 0, y - 6, Draw.W, 12, 120, 0.35);
    }
}
