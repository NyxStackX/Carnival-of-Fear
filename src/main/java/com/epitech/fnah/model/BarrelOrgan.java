package com.epitech.fnah.model;

/**
 * Orgue de barbarie de la fosse d'orchestre (caméra 8). Tant qu'il joue, Pépin
 * reste dans sa boîte. Il se vide tout seul et le joueur doit le remonter en
 * maintenant la manivelle depuis la caméra.
 */
public class BarrelOrgan {

    /** Charge maximale, en pourcentage. */
    public static final double MAX_CHARGE = 100.0;
    /** Charge gagnée par seconde de manivelle. */
    public static final double WIND_RATE = 20.0;

    private double charge = MAX_CHARGE;
    private boolean winding;

    /** Crée un orgue plein. */
    public BarrelOrgan() {
    }

    /**
     * Remonte l'orgue pendant une durée donnée.
     *
     * @param deltaSeconds durée de manivelle, en secondes
     * @throws IllegalArgumentException si la durée est négative
     */
    public void wind(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        charge = Math.min(MAX_CHARGE, charge + WIND_RATE * deltaSeconds);
    }

    /**
     * Vide l'orgue d'une quantité donnée.
     *
     * @param amount charge perdue, en pourcentage
     * @throws IllegalArgumentException si la quantité est négative
     */
    public void drain(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        charge = Math.max(0.0, charge - amount);
    }

    /**
     * Renvoie la charge restante.
     *
     * @return charge entre 0 et {@value #MAX_CHARGE}
     */
    public double getCharge() {
        return charge;
    }

    /**
     * Indique si l'orgue s'est arrêté.
     *
     * @return {@code true} si la charge est nulle
     */
    public boolean isEmpty() {
        return charge == 0.0;
    }

    /**
     * Indique si le joueur tient la manivelle.
     *
     * @return {@code true} si la manivelle est tenue
     */
    public boolean isWinding() {
        return winding;
    }

    /**
     * Prend ou lâche la manivelle.
     *
     * @param winding {@code true} pour tourner la manivelle
     */
    public void setWinding(boolean winding) {
        this.winding = winding;
    }

    /** Remet l'orgue plein et lâche la manivelle. */
    public void reset() {
        charge = MAX_CHARGE;
        winding = false;
    }
}
