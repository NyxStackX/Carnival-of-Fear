package com.epitech.fnah.model;

/**
 * Salles du chapiteau (GDD, section 5). Les liaisons entre salles sont
 * définies dans {@link MapGraph}.
 */
public enum Room {

    /** Piste centrale, point de départ de Bobo. */
    PISTE_CENTRALE("Piste centrale", "CAM 1"),
    /** Gradins, où Bobo se replie quand il est repoussé. */
    GRADINS("Gradins", "CAM 2"),
    /** Coulisses côté ouest. */
    COULISSES_OUEST("Coulisses ouest", "CAM 3"),
    /** Couloir derrière la porte gauche. */
    COULOIR_GAUCHE("Couloir gauche", "CAM 4"),
    /** Loges, lieu de l'incendie de 1994. */
    LOGES("Loges", "CAM 5"),
    /** Ménagerie aux cages vides. */
    MENAGERIE("Ménagerie", "CAM 6"),
    /** Couloir derrière la porte droite. */
    COULOIR_DROIT("Couloir droit", "CAM 7"),
    /** Fosse d'orchestre, reliée à la régie par une trappe. */
    FOSSE_ORCHESTRE("Fosse d'orchestre", "CAM 8"),
    /** Régie du joueur : un clown qui y entre attaque. */
    REGIE("Régie", null);

    private final String displayName;
    private final String cameraLabel;

    Room(String displayName, String cameraLabel) {
        this.displayName = displayName;
        this.cameraLabel = cameraLabel;
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
     * Renvoie le libellé de la caméra qui filme la salle, ou {@code null} pour la régie.
     *
     * @return libellé de la caméra qui filme la salle, ou {@code null} pour la régie
     */
    public String getCameraLabel() {
        return cameraLabel;
    }
}
