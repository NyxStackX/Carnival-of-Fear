package com.epitech.fnah.entity;

/**
 * Source de hasard des clowns. Passer par cette interface permet de remplacer
 * le hasard par des valeurs fixes dans les tests.
 */
@FunctionalInterface
public interface Dice {

    /**
     * Renvoie un entier entre 1 et 20 inclus.
     *
     * @return un entier entre 1 et 20 inclus
     */
    int rollD20();
}
