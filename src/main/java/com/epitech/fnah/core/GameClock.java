package com.epitech.fnah.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Horloge de la nuit : convertit le temps réel en heure de jeu.
 * Une heure de jeu dure {@value #SECONDS_PER_HOUR} secondes réelles,
 * et la nuit se termine à {@value #NIGHT_END_HOUR} h.
 */
public class GameClock {

    /** Secondes réelles par heure de jeu. */
    public static final int SECONDS_PER_HOUR = 60;
    /** Heure de fin de nuit (victoire). */
    public static final int NIGHT_END_HOUR = 6;

    private double realSeconds;
    private int lastNotifiedHour;

    private final List<IntConsumer> hourListeners = new ArrayList<>();

    /** Crée une horloge réglée sur 00:00. */
    public GameClock() {
        this.realSeconds = 0.0;
        this.lastNotifiedHour = 0;
    }

    /**
     * Fait avancer l'horloge et prévient les écouteurs de chaque nouvelle heure.
     *
     * @param deltaSeconds temps réel écoulé, en secondes
     * @throws IllegalArgumentException si {@code deltaSeconds} est négatif
     */
    public void update(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        this.realSeconds += deltaSeconds;
        int currentHour = getHour();
        while (lastNotifiedHour < currentHour) {
            lastNotifiedHour++;
            notifyHour(lastNotifiedHour);
        }
    }

    /**
     * Renvoie l'heure de jeu, de 0 à {@value #NIGHT_END_HOUR}.
     *
     * @return heure de jeu, de 0 à {@value #NIGHT_END_HOUR}
     */
    public int getHour() {
        int h = (int) (realSeconds / SECONDS_PER_HOUR);
        return Math.min(h, NIGHT_END_HOUR);
    }

    /**
     * Renvoie la minute de jeu dans l'heure courante, de 0 à 59.
     *
     * @return minute de jeu dans l'heure courante, de 0 à 59
     */
    public int getMinute() {
        double hourFraction = (realSeconds / SECONDS_PER_HOUR) - getHour();
        return (int) (hourFraction * 60);
    }

    /**
     * Renvoie l'heure au format "HH:MM".
     *
     * @return l'heure au format "HH:MM"
     */
    public String getFormattedTime() {
        return String.format("%02d:%02d", getHour(), getMinute());
    }

    /**
     * Renvoie {@code true} si {@value #NIGHT_END_HOUR} h est atteint.
     *
     * @return {@code true} si {@value #NIGHT_END_HOUR} h est atteint
     */
    public boolean isNightOver() {
        return getHour() >= NIGHT_END_HOUR;
    }

    /**
     * Renvoie les secondes réelles écoulées depuis le début de la nuit.
     *
     * @return secondes réelles écoulées depuis le début de la nuit
     */
    public double getElapsedSeconds() {
        return realSeconds;
    }

    /** Remet l'horloge sur 00:00. */
    public void reset() {
        realSeconds = 0.0;
        lastNotifiedHour = 0;
    }

    /**
     * Enregistre un écouteur de changement d'heure.
     *
     * @param listener appelé avec la nouvelle heure (1 à 6) à chaque changement d'heure
     */
    public void onHourChange(IntConsumer listener) {
        hourListeners.add(listener);
    }

    private void notifyHour(int hour) {
        for (IntConsumer l : hourListeners) {
            l.accept(hour);
        }
    }
}
