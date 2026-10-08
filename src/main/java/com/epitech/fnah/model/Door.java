package com.epitech.fnah.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Porte de la régie. Une porte fermée bloque les clowns mais consomme de l'énergie.
 */
public class Door implements PowerConsumer {

    /** Côté de la régie où se trouve la porte. */
    public enum Side {
        /** Porte gauche, donnant sur le couloir gauche. */
        LEFT("Porte gauche", Room.COULOIR_GAUCHE),
        /** Porte droite, donnant sur le couloir droit. */
        RIGHT("Porte droite", Room.COULOIR_DROIT);

        private final String displayName;
        private final Room corridor;

        Side(String displayName, Room corridor) {
            this.displayName = displayName;
            this.corridor = corridor;
        }

        /**
         * Renvoie le nom affiché de la porte.
         *
         * @return nom affiché de la porte
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Renvoie le couloir situé derrière la porte.
         *
         * @return couloir situé derrière la porte
         */
        public Room getCorridor() {
            return corridor;
        }
    }

    /** Points d'usage ajoutés à la consommation quand la porte est fermée. */
    public static final int POWER_USAGE_WHEN_CLOSED = 1;

    private final Side side;
    private boolean closed;
    private final List<Consumer<Door>> toggleListeners = new ArrayList<>();

    /**
     * Crée une porte ouverte.
     *
     * @param side côté de la régie
     */
    public Door(Side side) {
        this.side = Objects.requireNonNull(side, "side");
        this.closed = false;
    }

    /** Ferme la porte si elle est ouverte, l'ouvre sinon. */
    public void toggle() {
        closed = !closed;
        for (Consumer<Door> listener : toggleListeners) {
            listener.accept(this);
        }
    }

    /**
     * Renvoie {@code true} si la porte est fermée.
     *
     * @return {@code true} si la porte est fermée
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Renvoie le côté de la régie où se trouve la porte.
     *
     * @return côté de la régie
     */
    public Side getSide() {
        return side;
    }

    /**
     * Renvoie la consommation de la porte : {@value #POWER_USAGE_WHEN_CLOSED} si elle est fermée, 0 sinon.
     *
     * @return {@value #POWER_USAGE_WHEN_CLOSED} si fermée, 0 si ouverte
     */
    @Override
    public int getPowerUsage() {
        return closed ? POWER_USAGE_WHEN_CLOSED : 0;
    }

    /** Rouvre la porte en début de nuit, sans prévenir les écouteurs. */
    public void reset() {
        closed = false;
    }

    /**
     * Enregistre un écouteur appelé à chaque ouverture ou fermeture.
     *
     * @param listener reçoit la porte qui vient de changer d'état
     */
    public void onToggle(Consumer<Door> listener) {
        toggleListeners.add(Objects.requireNonNull(listener, "listener"));
    }
}
