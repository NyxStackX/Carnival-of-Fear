package com.epitech.fnah.model;

/**
 * La régie, où se trouve le joueur. Elle regroupe les équipements qui
 * consomment de l'énergie (portes, lampes des couloirs, tablette des caméras)
 * et l'orgue de barbarie qui garde Pépin dans sa boîte.
 */
public class Office implements PowerConsumer {

    private final Door leftDoor = new Door(Door.Side.LEFT);
    private final Door rightDoor = new Door(Door.Side.RIGHT);
    private final Light leftLight = new Light(Door.Side.LEFT);
    private final Light rightLight = new Light(Door.Side.RIGHT);
    private final CameraSystem cameras = new CameraSystem();
    private final BarrelOrgan organ = new BarrelOrgan();

    /** Crée une régie aux portes ouvertes, lampes éteintes, tablette baissée. */
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
     * Renvoie la porte droite.
     *
     * @return la porte droite
     */
    public Door getRightDoor() {
        return rightDoor;
    }

    /**
     * Renvoie la lampe du couloir gauche.
     *
     * @return la lampe gauche
     */
    public Light getLeftLight() {
        return leftLight;
    }

    /**
     * Renvoie la lampe du couloir droit.
     *
     * @return la lampe droite
     */
    public Light getRightLight() {
        return rightLight;
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
     * Renvoie l'orgue de barbarie de la fosse d'orchestre.
     *
     * @return l'orgue
     */
    public BarrelOrgan getOrgan() {
        return organ;
    }

    /**
     * Renvoie la somme des consommations des équipements de la régie.
     *
     * @return la somme des consommations des équipements de la régie
     */
    @Override
    public int getPowerUsage() {
        return leftDoor.getPowerUsage() + rightDoor.getPowerUsage()
                + leftLight.getPowerUsage() + rightLight.getPowerUsage() + cameras.getPowerUsage();
    }

    /**
     * Coupure de courant : les portes s'ouvrent, les lampes s'éteignent,
     * la tablette retombe et la manivelle est lâchée.
     */
    public void cutPower() {
        leftDoor.forceOpen();
        rightDoor.forceOpen();
        leftLight.setOn(false);
        rightLight.setOn(false);
        organ.setWinding(false);
        if (cameras.isOpen()) {
            cameras.toggleTablet();
        }
    }

    /** Remet la régie dans son état de début de nuit (portes ouvertes, tablette baissée, orgue plein). */
    public void reset() {
        leftDoor.reset();
        rightDoor.reset();
        leftLight.reset();
        rightLight.reset();
        cameras.reset();
        organ.reset();
    }
}
