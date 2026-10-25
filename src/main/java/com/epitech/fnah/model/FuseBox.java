package com.epitech.fnah.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Boîte à fusibles de la régie. Une surtension peut faire sauter un circuit
 * (caméras ou lampes des couloirs) : il ne marche plus tant que le joueur n'a
 * pas maintenu le levier pendant {@value #RESET_TIME} secondes, ce qui fait du bruit.
 */
public class FuseBox {

    /** Circuits protégés par un fusible. */
    public enum Circuit {
        /** La tablette des caméras. */
        CAMERAS,
        /** Les lampes des deux couloirs. */
        LIGHTS
    }

    /** Secondes de levier pour réarmer un circuit. */
    public static final double RESET_TIME = 3.0;

    private Circuit tripped;
    private boolean resetting;
    private double progress;
    private final List<Consumer<FuseBox>> listeners = new ArrayList<>();

    /** Crée une boîte à fusibles en état de marche. */
    public FuseBox() {
    }

    /**
     * Fait sauter un circuit. Sans effet si un circuit a déjà sauté.
     *
     * @param circuit circuit qui saute
     * @return {@code true} si le circuit vient de sauter
     */
    public boolean trip(Circuit circuit) {
        Objects.requireNonNull(circuit, "circuit");
        if (tripped != null) {
            return false;
        }
        tripped = circuit;
        progress = 0;
        notifyListeners();
        return true;
    }

    /**
     * Indique si un circuit a sauté.
     *
     * @param circuit circuit
     * @return {@code true} s'il ne marche plus
     */
    public boolean isTripped(Circuit circuit) {
        return tripped == circuit;
    }

    /**
     * Renvoie le circuit qui a sauté.
     *
     * @return le circuit, ou {@code null} si tout marche
     */
    public Circuit getTripped() {
        return tripped;
    }

    /**
     * Prend ou lâche le levier.
     *
     * @param resetting {@code true} tant que le joueur tient le levier
     */
    public void setResetting(boolean resetting) {
        this.resetting = resetting;
        if (!resetting) {
            progress = 0;
        }
    }

    /**
     * Indique si le joueur tient le levier.
     *
     * @return {@code true} s'il le tient
     */
    public boolean isResetting() {
        return resetting;
    }

    /**
     * Avance du réarmement, entre 0 et 1.
     *
     * @return proportion du réarmement
     */
    public double getProgress() {
        return progress / RESET_TIME;
    }

    /**
     * Fait avancer le réarmement si le joueur tient le levier.
     *
     * @param deltaSeconds temps écoulé, en secondes
     * @throws IllegalArgumentException si le temps est négatif
     */
    public void update(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        if (!resetting || tripped == null) {
            return;
        }
        progress += deltaSeconds;
        if (progress >= RESET_TIME) {
            tripped = null;
            progress = 0;
            resetting = false;
            notifyListeners();
        }
    }

    /**
     * Enregistre un écouteur prévenu quand un circuit saute ou est réarmé.
     *
     * @param listener reçoit la boîte à fusibles
     */
    public void onChange(Consumer<FuseBox> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Remet tous les circuits en marche, sans prévenir les écouteurs. */
    public void reset() {
        tripped = null;
        resetting = false;
        progress = 0;
    }

    private void notifyListeners() {
        for (Consumer<FuseBox> listener : listeners) {
            listener.accept(this);
        }
    }
}
