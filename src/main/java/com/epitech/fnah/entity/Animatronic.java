package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.core.Updatable;
import com.epitech.fnah.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Base commune à tous les clowns.
 *
 * <p>Toutes les {@code actionInterval} secondes, le clown lance un dé à 20 faces.
 * Si le résultat est inférieur ou égal à son niveau d'IA, il agit (méthode
 * {@link #act(GameContext)}, propre à chaque clown). Le niveau d'IA augmente
 * de 1 à 02:00 et à 04:00.</p>
 *
 * <p>Patron Template Method : {@link #update(double, GameContext)} fixe le
 * déroulé, les sous-classes ne définissent que l'action.</p>
 */
public abstract class Animatronic implements Updatable {

    /** Niveau d'IA maximal. */
    public static final int MAX_AI_LEVEL = 20;
    /** Heures auxquelles le niveau d'IA augmente de 1. */
    private static final int[] AI_BOOST_HOURS = {2, 4};

    private final String name;
    private final Room startRoom;
    private final double actionInterval;
    private final Dice dice;
    private final List<BiConsumer<Room, Room>> moveListeners = new ArrayList<>();

    private int baseAiLevel;
    private Room room;
    private double timer;

    /**
     * Crée un clown dans sa salle de départ.
     *
     * @param name           nom du clown
     * @param startRoom      salle de départ en début de nuit
     * @param baseAiLevel    niveau d'IA de départ, entre 0 et {@value #MAX_AI_LEVEL}
     * @param actionInterval secondes entre deux tentatives d'action, strictement positif
     * @param dice           source de hasard
     * @throws IllegalArgumentException si le niveau ou l'intervalle est invalide
     */
    protected Animatronic(String name, Room startRoom, int baseAiLevel, double actionInterval, Dice dice) {
        if (actionInterval <= 0) {
            throw new IllegalArgumentException("actionInterval must be > 0");
        }
        this.name = Objects.requireNonNull(name, "name");
        this.startRoom = Objects.requireNonNull(startRoom, "startRoom");
        this.dice = Objects.requireNonNull(dice, "dice");
        this.actionInterval = actionInterval;
        this.room = startRoom;
        this.baseAiLevel = checkAiLevel(baseAiLevel);
    }

    /**
     * Fait avancer le minuteur du clown et le fait agir à chaque intervalle réussi.
     *
     * @param deltaSeconds temps écoulé depuis la dernière mise à jour
     * @param ctx          état de la nuit en cours
     */
    @Override
    public final void update(double deltaSeconds, GameContext ctx) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        if (updateSpecialBehavior(deltaSeconds, ctx)) {
            return;
        }
        timer += deltaSeconds;
        while (timer >= actionInterval) {
            timer -= actionInterval;
            if (!isObserved(ctx) && rollAction(ctx.clock.getHour())) {
                act(ctx);
            }
        }
    }

    /**
     * Met à jour les minuteurs propres au clown avant son minuteur d'action.
     *
     * @param deltaSeconds temps écoulé
     * @param ctx état de la nuit
     * @return {@code true} si les actions normales sont en pause
     */
    protected boolean updateSpecialBehavior(double deltaSeconds, GameContext ctx) {
        return false;
    }

    /** Indique si ce clown est observé par la caméra de sa salle. */
    protected boolean isObserved(GameContext ctx) {
        return false;
    }

    /**
     * Action réalisée quand le jet de dé réussit (avancer, attaquer, reculer...).
     *
     * @param ctx état de la nuit en cours
     */
    protected abstract void act(GameContext ctx);

    /** Remet le clown dans sa salle de départ et vide son minuteur. */
    @Override
    public final void reset() {
        timer = 0;
        room = startRoom;
        resetSpecificBehavior();
    }

    /** Réinitialise l'état ajouté par le comportement propre au clown. */
    protected void resetSpecificBehavior() {
    }

    /**
     * Déplace le clown et prévient les écouteurs.
     *
     * @param target salle d'arrivée
     */
    protected final void moveTo(Room target) {
        Room from = room;
        room = Objects.requireNonNull(target, "target");
        for (BiConsumer<Room, Room> listener : moveListeners) {
            listener.accept(from, target);
        }
    }

    /**
     * Niveau d'IA effectif à une heure donnée. Un clown inactif (niveau 0) le reste.
     *
     * @param hour heure de jeu (0 à 6)
     * @return niveau d'IA entre 0 et {@value #MAX_AI_LEVEL}
     */
    public int getAiLevel(int hour) {
        if (baseAiLevel == 0) {
            return 0;
        }
        int level = baseAiLevel;
        for (int boostHour : AI_BOOST_HOURS) {
            if (hour >= boostHour) {
                level++;
            }
        }
        return Math.min(level, MAX_AI_LEVEL);
    }

    /**
     * Change le niveau d'IA de départ (réglage de la nuit).
     *
     * @param level nouveau niveau d'IA de départ, entre 0 et {@value #MAX_AI_LEVEL}
     * @throws IllegalArgumentException si le niveau est hors limites
     */
    public void setBaseAiLevel(int level) {
        this.baseAiLevel = checkAiLevel(level);
    }

    /**
     * Renvoie le niveau d'IA de départ.
     *
     * @return niveau d'IA de départ
     */
    public int getBaseAiLevel() {
        return baseAiLevel;
    }

    /**
     * Renvoie la salle où se trouve le clown.
     *
     * @return salle où se trouve le clown
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Renvoie le nom du clown.
     *
     * @return nom du clown
     */
    public String getName() {
        return name;
    }

    /**
     * Enregistre un écouteur appelé à chaque déplacement.
     *
     * @param listener reçoit la salle de départ puis la salle d'arrivée
     */
    public void onMove(BiConsumer<Room, Room> listener) {
        moveListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private static int checkAiLevel(int level) {
        if (level < 0 || level > MAX_AI_LEVEL) {
            throw new IllegalArgumentException("AI level must be between 0 and " + MAX_AI_LEVEL);
        }
        return level;
    }

    private boolean rollAction(int hour) {
        int level = getAiLevel(hour);
        return level > 0 && dice.rollD20() <= level;
    }

    protected final int rollD20() {
        return dice.rollD20();
    }
}
