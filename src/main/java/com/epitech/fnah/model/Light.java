package com.epitech.fnah.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Lampe d'un couloir, allumée tant que le joueur maintient la touche.
 * C'est le seul moyen de voir qui attend juste derrière la porte.
 */
public class Light implements PowerConsumer {

    /** Consommation d'énergie quand la lampe est allumée. */
    public static final int POWER_USAGE_WHEN_ON = 1;

    private final Door.Side side;
    private boolean on;
    private final List<Consumer<Light>> listeners = new ArrayList<>();

    /**
     * Crée une lampe éteinte.
     *
     * @param side côté du couloir éclairé
     */
    public Light(Door.Side side) {
        this.side = Objects.requireNonNull(side, "side");
    }

    /**
     * Allume ou éteint la lampe. Les écouteurs ne sont prévenus que si l'état change.
     *
     * @param on {@code true} pour allumer
     */
    public void setOn(boolean on) {
        if (this.on == on) {
            return;
        }
        this.on = on;
        for (Consumer<Light> listener : listeners) {
            listener.accept(this);
        }
    }

    /**
     * Indique si la lampe est allumée.
     *
     * @return {@code true} si elle est allumée
     */
    public boolean isOn() {
        return on;
    }

    /**
     * Renvoie le côté du couloir éclairé.
     *
     * @return côté de la lampe
     */
    public Door.Side getSide() {
        return side;
    }

    @Override
    public int getPowerUsage() {
        return on ? POWER_USAGE_WHEN_ON : 0;
    }

    /** Éteint la lampe sans prévenir les écouteurs (début de nuit). */
    public void reset() {
        on = false;
    }

    /**
     * Enregistre un écouteur appelé quand la lampe s'allume ou s'éteint.
     *
     * @param listener reçoit la lampe
     */
    public void onToggle(Consumer<Light> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }
}
