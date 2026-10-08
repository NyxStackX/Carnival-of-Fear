package com.epitech.fnah.model;

/**
 * La régie, où se trouve le joueur. Elle regroupe les équipements qui
 * consomment de l'énergie. Pour l'instant, seule la porte gauche existe ;
 * la porte droite, les lumières et la tablette viendront s'ajouter ici.
 */
public class Office implements PowerConsumer {

    private final Door leftDoor = new Door(Door.Side.LEFT);

    /** Crée une régie aux portes ouvertes. */
    public Office() {
    }

    /**
     * Renvoie la porte gauche.
     *
     * @return la porte gauche
     */
    public Door getLeftDoor() {
        return leftDoor;
    }

    /**
     * Renvoie la somme des consommations des équipements de la régie.
     *
     * @return la somme des consommations des équipements de la régie
     */
    @Override
    public int getPowerUsage() {
        return leftDoor.getPowerUsage();
    }

    /** Remet la régie dans son état de début de nuit (portes ouvertes). */
    public void reset() {
        leftDoor.reset();
    }
}
