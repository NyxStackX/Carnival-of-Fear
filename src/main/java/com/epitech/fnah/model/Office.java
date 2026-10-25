package com.epitech.fnah.model;

/**
 * La régie, où se trouve le joueur. Elle regroupe les équipements qui
 * consomment de l'énergie : la porte gauche et la tablette des caméras.
 * La porte droite et les lumières viendront s'ajouter ici.
 */
public class Office implements PowerConsumer {

    private final Door leftDoor = new Door(Door.Side.LEFT);
    private final CameraSystem cameras = new CameraSystem();

    /** Crée une régie aux portes ouvertes, tablette baissée. */
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
     * Renvoie la tablette des caméras.
     *
     * @return la tablette
     */
    public CameraSystem getCameras() {
        return cameras;
    }

    /**
     * Renvoie la somme des consommations des équipements de la régie.
     *
     * @return la somme des consommations des équipements de la régie
     */
    @Override
    public int getPowerUsage() {
        return leftDoor.getPowerUsage() + cameras.getPowerUsage();
    }

    /** Remet la régie dans son état de début de nuit (portes ouvertes, tablette baissée). */
    public void reset() {
        leftDoor.reset();
        cameras.reset();
    }
}
