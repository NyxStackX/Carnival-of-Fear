package com.epitech.fnah.config;

/**
 * Étoiles du menu principal, gagnées une fois pour toutes et enregistrées
 * dans la progression du joueur.
 */
public enum Achievement {

    /** Finir la nuit 5. Débloque aussi la nuit personnalisée. */
    LAST_SHOW("Le dernier spectacle", "Finir la nuit 5"),
    /** Finir une nuit sans jamais fermer une porte. */
    OPEN_DOORS("Portes ouvertes", "Finir une nuit sans fermer de porte"),
    /** Finir une nuit sans lever la tablette. */
    EYES_CLOSED("Les yeux fermés", "Finir une nuit sans les caméras"),
    /** Finir la nuit personnalisée avec tous les clowns au niveau 20. */
    MAX_CHALLENGE("20 / 20 / 20 / 20", "Finir la nuit personnalisée au maximum");

    private final String title;
    private final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * Renvoie le nom affiché de l'étoile.
     *
     * @return nom affiché
     */
    public String getTitle() {
        return title;
    }

    /**
     * Renvoie la condition pour obtenir l'étoile.
     *
     * @return condition, en une phrase
     */
    public String getDescription() {
        return description;
    }
}
