package com.epitech.fnah.view;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Palette de couleurs et outils de dessin communs à tous les écrans :
 * texte avec ombre, sprites, effets d'image de surveillance (grain, lignes,
 * vignettage).
 *
 * <p>Performance : les couches qui ne changent pas d'une image à l'autre
 * (dégradés, vignettage, lignes de balayage, fonds) sont calculées une seule
 * fois avec {@link #render} puis simplement recopiées. Sans carte graphique
 * (rendu logiciel, fréquent sous WSL), les recalculer 60 fois par seconde
 * faisait chuter le jeu sous les 30 images par seconde.</p>
 */
final class Draw {

    static final double W = GameApp.WIDTH;
    static final double H = GameApp.HEIGHT;

    static final Color RED = Color.web("#b01827");
    static final Color RED_DARK = Color.web("#5a1019");
    static final Color CREAM = Color.web("#e6dcc4");
    static final Color TEXT = Color.web("#d8cfc0");
    static final Color TEXT_DIM = Color.web("#8a8178");
    static final Color AMBER = Color.web("#ffb000");
    static final Color GOLD = Color.web("#b08d45");

    private static final Random RANDOM = new Random();
    private static final Map<String, Image> OVERLAYS = new HashMap<>();

    private Draw() {
    }

    /**
     * Dessine une fois dans une image hors écran, réutilisable ensuite.
     *
     * @param w       largeur
     * @param h       hauteur
     * @param painter dessin à effectuer
     * @return l'image obtenue, avec transparence
     */
    static Image render(double w, double h, Consumer<GraphicsContext> painter) {
        Canvas canvas = new Canvas(w, h);
        painter.accept(canvas.getGraphicsContext2D());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    /**
     * Voile d'image de surveillance plein écran : assombrissement, lignes de
     * balayage et vignettage, calculé une fois par combinaison puis recopié.
     */
    static void overlay(GraphicsContext gc, double shade, double scanlines, double vignette) {
        String key = shade + "/" + scanlines + "/" + vignette;
        Image image = OVERLAYS.computeIfAbsent(key, k -> render(W, H, g -> {
            if (shade > 0) {
                shade(g, shade);
            }
            if (scanlines > 0) {
                scanlines(g, scanlines);
            }
            vignette(g, vignette);
        }));
        gc.drawImage(image, 0, 0);
    }

    /**
     * Dessine une image déjà à la bonne taille, posée sur une ligne de sol.
     *
     * @param centerX centre horizontal
     * @param bottomY bas de l'image
     */
    static void spriteAt(GraphicsContext gc, Image image, double centerX, double bottomY) {
        if (image != null) {
            gc.drawImage(image, Math.round(centerX - image.getWidth() / 2), Math.round(bottomY - image.getHeight()));
        }
    }

    static void clear(GraphicsContext gc, Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, W, H);
    }

    static void shade(GraphicsContext gc, double alpha) {
        gc.setFill(Color.rgb(0, 0, 0, Math.max(0, Math.min(1, alpha))));
        gc.fillRect(0, 0, W, H);
    }

    static void text(GraphicsContext gc, String value, double x, double y, Font font, Color color, TextAlignment align) {
        gc.setTextAlign(align);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(font);
        gc.setFill(color);
        gc.fillText(value, x, y);
    }

    /** Texte avec une ombre portée, pour qu'il reste lisible sur une image. */
    static void shadowText(GraphicsContext gc, String value, double x, double y, Font font, Color color,
                           TextAlignment align) {
        text(gc, value, x + 3, y + 3, font, Color.rgb(0, 0, 0, 0.8), align);
        text(gc, value, x, y, font, color, align);
    }

    /**
     * Dessine un sprite en gardant ses proportions, posé sur une ligne de sol.
     *
     * @param centerX centre horizontal
     * @param bottomY bas de l'image (les pieds)
     * @param height  hauteur affichée de l'image entière
     */
    static void sprite(GraphicsContext gc, Image image, double centerX, double bottomY, double height) {
        if (image == null) {
            return;
        }
        double width = height * image.getWidth() / image.getHeight();
        gc.drawImage(image, centerX - width / 2, bottomY - height, width, height);
    }

    /** Image recadrée pour remplir un rectangle sans être déformée. */
    static void cover(GraphicsContext gc, Image image, double x, double y, double w, double h, double zoom) {
        if (image == null) {
            return;
        }
        double scale = Math.max(w / image.getWidth(), h / image.getHeight()) * zoom;
        double sw = w / scale;
        double sh = h / scale;
        double sx = (image.getWidth() - sw) / 2;
        double sy = (image.getHeight() - sh) / 2;
        gc.drawImage(image, sx, sy, sw, sh, x, y, w, h);
    }

    static void vignette(GraphicsContext gc, double strength) {
        gc.setFill(new RadialGradient(0, 0, W / 2, H / 2, W * 0.72, false, CycleMethod.NO_CYCLE,
                new Stop(0.4, Color.TRANSPARENT), new Stop(1.0, Color.rgb(0, 0, 0, strength))));
        gc.fillRect(0, 0, W, H);
    }

    static void scanlines(GraphicsContext gc, double alpha) {
        gc.setStroke(Color.rgb(0, 0, 0, alpha));
        gc.setLineWidth(1);
        for (double y = 0; y < H; y += 4) {
            gc.strokeLine(0, y, W, y);
        }
    }

    static void grain(GraphicsContext gc, int dots, double maxAlpha) {
        grain(gc, 0, 0, W, H, dots, maxAlpha);
    }

    static void grain(GraphicsContext gc, double x, double y, double w, double h, int dots, double maxAlpha) {
        for (int i = 0; i < dots; i++) {
            gc.setFill(Color.gray(RANDOM.nextDouble(), RANDOM.nextDouble() * maxAlpha));
            gc.fillRect(x + RANDOM.nextDouble() * w, y + RANDOM.nextDouble() * h, 3, 3);
        }
    }

    static double random(double min, double max) {
        return min + RANDOM.nextDouble() * (max - min);
    }

    static boolean inside(double x, double y, double rx, double ry, double rw, double rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }
}
