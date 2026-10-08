package com.epitech.fnah.entity;

import java.util.Objects;
import java.util.Random;

/**
 * Dé à 20 faces basé sur {@link Random}. Utilisé en jeu.
 */
public class RandomDice implements Dice {

    private final Random random;

    /** Crée un dé avec une graine aléatoire. */
    public RandomDice() {
        this(new Random());
    }

    /**
     * Crée un dé à partir d'un générateur donné (utile pour rejouer une partie).
     *
     * @param random générateur à utiliser
     */
    public RandomDice(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public int rollD20() {
        return random.nextInt(1, 21);
    }
}
