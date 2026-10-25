package com.epitech.fnah.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Coffre de l'ancien directeur, sous le bureau. Son code à trois chiffres
 * change chaque nuit ; chaque chiffre est griffonné sur le mur d'une salle,
 * visible seulement depuis sa caméra. Ouvert, il contient une batterie de
 * secours et une page du journal de Henry.
 */
public class Safe {

    /** Nombre de chiffres du code. */
    public static final int DIGITS = 3;
    /** Salles où sont écrits les chiffres, dans l'ordre du code. */
    public static final List<Room> CLUE_ROOMS = List.of(Room.GRADINS, Room.LOGES, Room.MENAGERIE);

    /** Résultat d'un chiffre tapé. */
    public enum Result {
        /** Le chiffre est noté, il en manque encore. */
        PARTIAL,
        /** Code faux : la saisie est effacée. */
        WRONG,
        /** Code juste : le coffre s'ouvre. */
        OPENED,
        /** Le coffre est déjà ouvert : rien ne se passe. */
        ALREADY_OPEN
    }

    private final int[] code = new int[DIGITS];
    private final boolean[] discovered = new boolean[DIGITS];
    private final int[] entry = new int[DIGITS];
    private int typed;
    private boolean open;

    /** Crée un coffre fermé, code 000. */
    public Safe() {
    }

    /**
     * Change le code (début de nuit) et oublie les indices trouvés.
     *
     * @param digits trois chiffres entre 0 et 9
     * @throws IllegalArgumentException si le code est invalide
     */
    public void setCode(int... digits) {
        Objects.requireNonNull(digits, "digits");
        if (digits.length != DIGITS) {
            throw new IllegalArgumentException("code must have " + DIGITS + " digits");
        }
        for (int d : digits) {
            checkDigit(d);
        }
        System.arraycopy(digits, 0, code, 0, DIGITS);
        Arrays.fill(discovered, false);
        open = false;
        typed = 0;
    }

    /**
     * Note qu'un chiffre a été vu sur une caméra.
     *
     * @param index rang du chiffre (0 à 2)
     * @return {@code true} s'il n'était pas encore connu
     */
    public boolean discover(int index) {
        checkIndex(index);
        boolean fresh = !discovered[index];
        discovered[index] = true;
        return fresh;
    }

    /**
     * Indique si un chiffre a déjà été trouvé.
     *
     * @param index rang du chiffre
     * @return {@code true} s'il est connu
     */
    public boolean isDiscovered(int index) {
        checkIndex(index);
        return discovered[index];
    }

    /**
     * Renvoie un chiffre du code.
     *
     * @param index rang du chiffre
     * @return le chiffre
     */
    public int digit(int index) {
        checkIndex(index);
        return code[index];
    }

    /**
     * Tape un chiffre sur le cadran.
     *
     * @param digit chiffre entre 0 et 9
     * @return ce qu'il se passe
     * @throws IllegalArgumentException si ce n'est pas un chiffre
     */
    public Result press(int digit) {
        checkDigit(digit);
        if (open) {
            return Result.ALREADY_OPEN;
        }
        entry[typed++] = digit;
        if (typed < DIGITS) {
            return Result.PARTIAL;
        }
        typed = 0;
        if (Arrays.equals(entry, code)) {
            open = true;
            return Result.OPENED;
        }
        return Result.WRONG;
    }

    /**
     * Renvoie le nombre de chiffres déjà tapés.
     *
     * @return entre 0 et 2
     */
    public int getTypedCount() {
        return typed;
    }

    /** Efface la saisie en cours. */
    public void clearEntry() {
        typed = 0;
    }

    /**
     * Indique si le coffre est ouvert.
     *
     * @return {@code true} s'il est ouvert
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Rang du chiffre écrit dans une salle.
     *
     * @param room salle
     * @return rang du chiffre, ou -1 si aucun chiffre n'y est écrit
     */
    public static int clueIndex(Room room) {
        return CLUE_ROOMS.indexOf(room);
    }

    private static void checkDigit(int digit) {
        if (digit < 0 || digit > 9) {
            throw new IllegalArgumentException("digit must be between 0 and 9");
        }
    }

    private static void checkIndex(int index) {
        if (index < 0 || index >= DIGITS) {
            throw new IllegalArgumentException("index must be between 0 and " + (DIGITS - 1));
        }
    }
}
