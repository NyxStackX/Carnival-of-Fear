package com.epitech.fnah.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
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

    void render(GraphicsContext gc, Font font, Font selectedFont) {
        for (int i = 0; i < options.size(); i++) {
            double cy = y + i * spacing;
            String label = options.get(i).label().get();
            if (i == selected) {
                double glow = 0.5 + 0.5 * Math.sin(pulse * 4);
                gc.setFill(Color.rgb(179, 32, 47, 0.18 + 0.12 * glow));
                gc.fillRect(x - 24, cy - spacing * 0.42, width, spacing * 0.84);
                gc.setFill(Draw.RED);
                gc.fillPolygon(new double[]{x - 16, x - 4, x - 16}, new double[]{cy - 9, cy, cy + 9}, 3);
                Draw.shadowText(gc, label, x + 8, cy, selectedFont, Draw.CREAM, TextAlignment.LEFT);
            } else {
                Draw.shadowText(gc, label, x, cy, font, Draw.TEXT_DIM, TextAlignment.LEFT);
            }
        }
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
