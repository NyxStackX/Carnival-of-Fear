package com.epitech.fnah.entity;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomDiceTest {

    @Test
    void rollsStayBetweenOneAndTwenty() {
        Dice dice = new RandomDice(new Random(42));
        for (int i = 0; i < 1000; i++) {
            int roll = dice.rollD20();
            assertTrue(roll >= 1 && roll <= 20, "roll out of range: " + roll);
        }
    }

    @Test
    void sameSeedGivesSameRolls() {
        Dice a = new RandomDice(new Random(7));
        Dice b = new RandomDice(new Random(7));
        for (int i = 0; i < 50; i++) {
            assertEquals(a.rollD20(), b.rollD20());
        }
    }

    @Test
    void defaultConstructorWorks() {
        int roll = new RandomDice().rollD20();
        assertTrue(roll >= 1 && roll <= 20);
    }
}
