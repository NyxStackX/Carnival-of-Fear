package com.epitech.fnah.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Réserve d'électricité de la régie.
 *
 * <p>L'énergie part de {@value #MAX_ENERGY} % et baisse en continu de
 * {@value #DRAIN_PER_USAGE_PER_SECOND} % par seconde et par point d'usage.
 * L'usage vaut {@value #BASE_USAGE} (ventilation, veilleuse) plus la consommation
 * de chaque {@link PowerConsumer} branché. À 0 %, la panne est déclenchée une seule fois.</p>
 */
public class PowerSystem {

    /** Énergie au début de chaque nuit, en pourcentage. */
    public static final double MAX_ENERGY = 100.0;
    /** Perte d'énergie par seconde et par point d'usage, en pourcentage. */
    public static final double DRAIN_PER_USAGE_PER_SECOND = 0.15;
    /** Usage permanent de la régie, même sans aucun équipement actif. */
    public static final int BASE_USAGE = 1;

    private double energy;
    private double energyMultiplier;
    private boolean powerOut;

    private final List<PowerConsumer> consumers = new ArrayList<>();
    private final List<Consumer<PowerSystem>> powerOutListeners = new ArrayList<>();

    /** Crée une réserve pleine avec une consommation normale (multiplicateur 1). */
    public PowerSystem() {
        this(1.0);
    }

    /**
     * Crée une réserve pleine.
     *
     * @param energyMultiplier multiplicateur appliqué à la consommation (difficulté de la nuit)
     */
    public PowerSystem(double energyMultiplier) {
        this.energy = MAX_ENERGY;
        this.energyMultiplier = energyMultiplier;
        this.powerOut = false;
    }

    /**
     * Branche un équipement sur la réserve.
     *
     * @param c équipement dont la consommation s'ajoute à l'usage
     */
    public void addConsumer(PowerConsumer c) {
        consumers.add(c);
    }

    /**
     * Renvoie l'usage total : {@value #BASE_USAGE} plus la consommation de chaque équipement.
     *
     * @return usage total : {@value #BASE_USAGE} plus la consommation de chaque équipement
     */
    public int computeUsage() {
        int usage = BASE_USAGE;
        for (PowerConsumer c : consumers) {
            usage += c.getPowerUsage();
        }
        return usage;
    }

    /**
     * Fait baisser l'énergie selon l'usage actuel. Sans effet après la panne.
     *
     * @param deltaSeconds temps écoulé, en secondes
     * @throws IllegalArgumentException si {@code deltaSeconds} est négatif
     */
    public void update(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        if (powerOut) {
            return;
        }
        double drain = computeUsage()
                * DRAIN_PER_USAGE_PER_SECOND
                * energyMultiplier
                * deltaSeconds;
        energy = Math.max(0.0, energy - drain);
        if (energy == 0.0 && !powerOut) {
            powerOut = true;
            notifyPowerOut();
        }
    }

    /**
     * Renvoie l'énergie restante, entre 0 et {@value #MAX_ENERGY}.
     *
     * @return énergie restante, entre 0 et {@value #MAX_ENERGY}
     */
    public double getEnergy() { return energy; }

    /**
     * Recharge la réserve (batterie de secours), sans dépasser {@value #MAX_ENERGY}.
     * Sans effet après une coupure : le disjoncteur général est grillé.
     *
     * @param amount énergie ajoutée, en pourcentage
     * @throws IllegalArgumentException si la quantité est négative
     */
    public void recharge(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        if (!powerOut) {
            energy = Math.min(MAX_ENERGY, energy + amount);
        }
    }

    /**
     * Renvoie {@code true} si l'énergie est tombée à 0.
     *
     * @return {@code true} si l'énergie est tombée à 0
     */
    public boolean isPowerOut() { return powerOut; }

    /**
     * Change la difficulté de consommation de la nuit.
     *
     * @param m nouveau multiplicateur de consommation
     */
    public void setEnergyMultiplier(double m) { this.energyMultiplier = m; }

    /**
     * Enregistre un écouteur de panne.
     *
     * @param listener appelé une seule fois quand l'énergie atteint 0
     */
    public void onPowerOut(Consumer<PowerSystem> listener) {
        powerOutListeners.add(listener);
    }

    /** Remet l'énergie au maximum et annule la panne. Le multiplicateur est conservé. */
    public void reset() {
        energy = MAX_ENERGY;
        powerOut = false;
    }

    private void notifyPowerOut() {
        for (Consumer<PowerSystem> l : powerOutListeners) {
            l.accept(this);
        }
    }
}
