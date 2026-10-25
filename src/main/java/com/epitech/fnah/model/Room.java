package com.epitech.fnah.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Salles du chapiteau (GDD, section 5). Les liaisons entre salles sont
 * définies dans {@link MapGraph}. Chaque salle, sauf la régie, est filmée
 * par une caméra numérotée de 1 à {@value #CAMERA_COUNT}.
 */
public enum Room {

    /** Piste centrale, point de départ de Bobo. */
    PISTE_CENTRALE("Piste centrale", 1),
    /** Gradins, où Bobo se replie quand il est repoussé. */
    GRADINS("Gradins", 2),
    /** Coulisses côté ouest. */
    COULISSES_OUEST("Coulisses ouest", 3),
    /** Couloir derrière la porte gauche. */
    COULOIR_GAUCHE("Couloir gauche", 4),
    /** Loges, lieu de l'incendie de 1994. */
    LOGES("Loges", 5),
    /** Ménagerie aux cages vides. */
    MENAGERIE("Ménagerie", 6),
    /** Couloir derrière la porte droite. */
    COULOIR_DROIT("Couloir droit", 7),
    /** Fosse d'orchestre, reliée à la régie par une trappe. */
    FOSSE_ORCHESTRE("Fosse d'orchestre", 8),
    /** Régie du joueur : un clown qui y entre attaque. Aucune caméra. */
    REGIE("Régie", 0);

    /** Nombre de caméras du chapiteau. */
    public static final int CAMERA_COUNT = 8;

    private final String displayName;
    private final int cameraNumber;

    Room(String displayName, int cameraNumber) {
        this.displayName = displayName;
        this.cameraNumber = cameraNumber;
    }

    /**
     * Renvoie le nom affiché au joueur.
     *
     * @return nom affiché au joueur
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Renvoie le numéro de la caméra qui filme la salle.
     *
     * @return numéro de 1 à {@value #CAMERA_COUNT}, ou 0 pour la régie
     */
    public int getCameraNumber() {
        return cameraNumber;
    }

    /**
     * Indique si une caméra filme la salle.
     *
     * @return {@code true} pour toutes les salles sauf la régie
     */
    public boolean hasCamera() {
        return cameraNumber > 0;
    }

    /**
     * Renvoie le libellé de la caméra qui filme la salle, ou {@code null} pour la régie.
     *
     * @return libellé du type "CAM 4", ou {@code null} pour la régie
     */
    public String getCameraLabel() {
        return hasCamera() ? "CAM " + cameraNumber : null;
    }

    /**
     * Retrouve la salle filmée par une caméra.
     *
     * @param number numéro de caméra
     * @return la salle, ou vide si aucune caméra ne porte ce numéro
     */
    public static Optional<Room> fromCameraNumber(int number) {
        return Arrays.stream(values()).filter(r -> r.hasCamera() && r.cameraNumber == number).findFirst();
    }

    /**
     * Liste les salles filmées, dans l'ordre des caméras.
     *
     * @return les {@value #CAMERA_COUNT} salles équipées d'une caméra
     */
    public static List<Room> withCameras() {
        return Arrays.stream(values()).filter(Room::hasCamera).toList();
    }
}
