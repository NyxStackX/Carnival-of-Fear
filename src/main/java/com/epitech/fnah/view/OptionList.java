package com.epitech.fnah.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Liste verticale d'options navigable au clavier (Haut, Bas, Entrée, Gauche,
 * Droite) et à la souris (survol, clic). Utilisée par le menu et les écrans de fin.
 */
final class OptionList {

    private record Option(Supplier<String> label, Runnable action, IntConsumer adjust) {
    }

    private final List<Option> options = new ArrayList<>();
    private final double x;
    private final double y;
    private final double spacing;
    private final double width;
    private final Runnable onMove;
    private final Runnable onSelect;
    private int selected;
    private double pulse;

    /**
     * @param x        bord gauche des options
     * @param y        centre vertical de la première option
     * @param spacing  écart vertical entre deux options
     * @param width    largeur de la zone cliquable
     * @param onMove   son joué quand la sélection change
     * @param onSelect son joué quand une option est validée
     */
    OptionList(double x, double y, double spacing, double width, Runnable onMove, Runnable onSelect) {
        this.x = x;
        this.y = y;
        this.spacing = spacing;
        this.width = width;
        this.onMove = onMove;
        this.onSelect = onSelect;
    }

    OptionList add(Supplier<String> label, Runnable action) {
        return add(label, action, null);
    }

    OptionList add(Supplier<String> label, Runnable action, IntConsumer adjust) {
        options.add(new Option(label, action, adjust));
        return this;
    }

    void clear() {
        options.clear();
        selected = 0;
    }

    void update(double dt) {
        pulse += dt;
    }

    void reset() {
        selected = 0;
    }

    int getSelected() {
        return selected;
    }

    void onKey(KeyCode code) {
        switch (code) {
            case UP -> move(-1);
            case DOWN -> move(1);
            case LEFT -> adjust(-1);
            case RIGHT -> adjust(1);
            case ENTER -> activate(selected);
            default -> {
                // Les autres touches sont gérées par l'écran.
            }
        }
    }

    void onMouseMove(double mx, double my) {
        int index = indexAt(mx, my);
        if (index >= 0 && index != selected) {
            selected = index;
            onMove.run();
        }
    }

    void onClick(double mx, double my) {
        int index = indexAt(mx, my);
        if (index >= 0) {
            selected = index;
            activate(index);
        }
    }

    /**
     * Dessine les options. Une option réglable peut séparer son nom et sa valeur
     * par {@code |} (ex. {@code "NUIT|3"}) : la valeur s'affiche à droite, entre
     * deux flèches.
     */
    void render(GraphicsContext gc, Font font, Font selectedFont) {
        double left = x - 24;
        for (int i = 0; i < options.size(); i++) {
            double cy = y + i * spacing;
            Option option = options.get(i);
            String label = option.label().get();
            String value = null;
            int bar = label.indexOf('|');
            if (bar >= 0) {
                value = label.substring(bar + 1);
                label = label.substring(0, bar);
            }
            boolean active = i == selected;
            double h = spacing * 0.8;
            if (active) {
                double glow = 0.5 + 0.5 * Math.sin(pulse * 4);
                double right = left + width;
                gc.setFill(new LinearGradient(left, 0, right, 0, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(150, 22, 36, 0.92)), new Stop(0.75, Color.rgb(110, 16, 28, 0.75)),
                        new Stop(1, Color.rgb(110, 16, 28, 0.0))));
                gc.fillPolygon(new double[]{left, right, right - 14, right},
                        new double[]{cy - h / 2, cy - h / 2, cy, cy + h / 2}, 4);
                gc.fillRect(left, cy - h / 2, width - 14, h);
                gc.setFill(Color.rgb(201, 162, 74, 0.55 + 0.35 * glow));
                gc.fillRect(left, cy - h / 2, 3, h);
                Ui.diamond(gc, x - 6, cy, 5, Draw.GOLD);
                Draw.shadowText(gc, label, x + 10, cy, selectedFont, Draw.CREAM, TextAlignment.LEFT);
            } else {
                Ui.diamond(gc, x - 6, cy, 3, Color.rgb(201, 162, 74, 0.35));
                Draw.shadowText(gc, label, x + 4, cy, font, Draw.TEXT_DIM, TextAlignment.LEFT);
            }
            if (value != null) {
                double vx = left + width - 70;
                Font f = active ? selectedFont : font;
                Color c = active ? Draw.CREAM : Draw.TEXT_DIM;
                Draw.shadowText(gc, value, vx, cy, f, active ? Draw.GOLD : Draw.TEXT_DIM, TextAlignment.CENTER);
                double half = Math.max(22, Ui.width(value, f) / 2 + 14);
                if (option.adjust() != null) {
                    chevron(gc, vx - half, cy, -1, c);
                    chevron(gc, vx + half, cy, 1, c);
                }
            }
        }
    }

    private static void chevron(GraphicsContext gc, double cx, double cy, int dir, Color color) {
        gc.setFill(color);
        gc.fillPolygon(new double[]{cx - dir * 4, cx + dir * 4, cx - dir * 4},
                new double[]{cy - 6, cy, cy + 6}, 3);
    }

    private void move(int step) {
        selected = Math.floorMod(selected + step, options.size());
        onMove.run();
    }

    private void adjust(int step) {
        IntConsumer adjust = options.get(selected).adjust();
        if (adjust != null) {
            adjust.accept(step);
            onMove.run();
        }
    }

    private void activate(int index) {
        Runnable action = options.get(index).action();
        if (action != null) {
            try {
                onSelect.run();
            } finally {
                action.run();
            }
        }
    }

    private int indexAt(double mx, double my) {
        for (int i = 0; i < options.size(); i++) {
            double cy = y + i * spacing;
            if (Draw.inside(mx, my, x - 24, cy - spacing / 2, width, spacing)) {
                return i;
            }
        }
        return -1;
    }
}
