package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.model.Room;

/**
 * Mimi, la contorsionniste. Elle avance par le côté droit du chapiteau
 * (loges, ménagerie, couloir droit) seulement quand personne ne la regarde.
 * Arrivée au couloir droit porte ouverte, elle se coince dans l'embrasure et
 * bloque la porte : il faut la regarder sur la caméra 7 pour la faire reculer.
 */
public final class Mimi extends Animatronic {

    /** Nom de Mimi. */
    public static final String NAME = "Mimi";
    /** Secondes entre deux tentatives d'action. */
    public static final double ACTION_INTERVAL = 4.0;
    /** Secondes dans l'embrasure avant d'attaquer. */
    public static final double EMBRASURE_TIMEOUT = 3.0;
    /** Salle de départ. */
    public static final Room START_ROOM = Room.LOGES;
    /** Salle où elle recule quand elle est repoussée. */
    public static final Room RETREAT_ROOM = Room.MENAGERIE;

    private double embrasureTimer;
    private boolean atEmbrasure;

    /**
     * Crée Mimi dans les loges.
     *
     * @param baseAiLevel niveau d'IA de départ
     * @param dice        source de hasard
     */
    public Mimi(int baseAiLevel, Dice dice) {
        super(NAME, START_ROOM, baseAiLevel, ACTION_INTERVAL, dice);
    }

    @Override
    protected boolean updateSpecialBehavior(double deltaSeconds, GameContext ctx) {
        if (atEmbrasure) {
            if (ctx.office.getCameras().isWatching(Room.COULOIR_DROIT)) {
                releaseEmbrasure(ctx);
                moveTo(RETREAT_ROOM);
                return true;
            }
            embrasureTimer -= deltaSeconds;
            if (embrasureTimer <= 0) {
                releaseEmbrasure(ctx);
                moveTo(Room.REGIE);
                ctx.attackPlayer(getName());
            }
            return true;
        }
        return isObserved(ctx);
    }

    @Override
    protected int hearingBonus(GameContext ctx) {
        return ctx.office.getNoise().hearingBonus();
    }

    @Override
    protected boolean isObserved(GameContext ctx) {
        return ctx.office.getCameras().isWatching(getRoom());
    }

    @Override
    protected void act(GameContext ctx) {
        if (getRoom() == Room.LOGES) {
            moveTo(Room.MENAGERIE);
        } else if (getRoom() == Room.MENAGERIE) {
            if (ctx.office.getRightDoor().isClosed()) {
                moveTo(Room.LOGES);
                return;
            }
            moveTo(Room.COULOIR_DROIT);
        } else if (getRoom() == Room.COULOIR_DROIT) {
            if (ctx.office.getRightDoor().isClosed()) {
                moveTo(RETREAT_ROOM);
            } else {
                ctx.office.getRightDoor().setBlocked(true);
                atEmbrasure = true;
                embrasureTimer = EMBRASURE_TIMEOUT;
            }
        }
    }

    @Override
    protected void resetSpecificBehavior() {
        atEmbrasure = false;
        embrasureTimer = 0;
    }

    /**
     * Indique si Mimi est coincée dans l'embrasure de la porte droite.
     *
     * @return {@code true} si elle bloque la porte
     */
    public boolean isAtEmbrasure() {
        return atEmbrasure;
    }

    /**
     * Renvoie le temps restant avant son attaque depuis l'embrasure.
     *
     * @return secondes restantes, ou 0 si elle n'y est pas
     */
    public double getEmbrasureTimeRemaining() {
        return atEmbrasure ? Math.max(0, embrasureTimer) : 0;
    }

    private void releaseEmbrasure(GameContext ctx) {
        atEmbrasure = false;
        embrasureTimer = 0;
        ctx.office.getRightDoor().setBlocked(false);
    }
}
