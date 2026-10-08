package com.epitech.fnah.model;

/**
 * Équipement qui consomme de l'énergie (porte, lumière, tablette...).
 */
public interface PowerConsumer {

    /**
     * Renvoie les points d'usage ajoutés à la consommation (0 si l'équipement est inactif).
     *
     * @return points d'usage ajoutés à la consommation, 0 si l'équipement est inactif
     */
    int getPowerUsage();
}
