package com.epitech.fnah.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PowerSystem {

    public static final double MAX_ENERGY = 100.0;
    public static final double DRAIN_PER_USAGE_PER_SECOND = 0.1;
    public static final int BASE_USAGE = 1;

    private double energy;
    private double energyMultiplier;
    private boolean powerOut;

    private final List<PowerConsumer> consumers = new ArrayList<>();
    private final List<Consumer<PowerSystem>> powerOutListeners = new ArrayList<>();

    public PowerSystem() {
        this(1.0);
    }

    public PowerSystem(double energyMultiplier) {
        this.energy = MAX_ENERGY;
        this.energyMultiplier = energyMultiplier;
        this.powerOut = false;
    }

    public void addConsumer(PowerConsumer c) {
        consumers.add(c);
    }

    public int computeUsage() {
        int usage = BASE_USAGE;
        for (PowerConsumer c : consumers) {
            usage += c.getPowerUsage();
        }
        return usage;
    }

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

    public double getEnergy() { return energy; }

    public boolean isPowerOut() { return powerOut; }

    public void setEnergyMultiplier(double m) { this.energyMultiplier = m; }

    public void onPowerOut(Consumer<PowerSystem> listener) {
        powerOutListeners.add(listener);
    }

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