package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.Updatable;

import java.util.Objects;

/**
 * Henry, l'ancien directeur du cirque, qui n'apparaît qu'à la coupure de courant.
 *
 * <p>Quand l'énergie tombe à 0 %, tout s'éteint ({@link Phase#DARK}). Après
 * quelques secondes, ses yeux s'allument dans l'encadrement de la porte gauche
 * et il joue sa mélodie ({@link Phase#MELODY}). Quand elle s'arrête, il entre.
 * Le joueur ne survit que si 06:00 arrive avant. Les durées dépendent du dé.</p>
 */
public final class Henry implements Updatable {

    /** Nom de Henry. */
    public static final String NAME = "Henry";
    /** Dossier de ses images. */
    public static final String SPRITE_ID = "henry";
    /** Durée minimale du noir complet avant la mélodie, en secondes. */
    public static final double DARK_BASE = 4.0;
    /** Durée minimale de la mélodie avant l'attaque, en secondes. */
    public static final double MELODY_BASE = 6.0;
    /** Secondes ajoutées par point de dé (d20). */
    public static final double SECONDS_PER_ROLL = 0.5;

    /** Étapes de la coupure de courant. */
    public enum Phase {
        /** Le courant est encore là. */
        WAITING,
        /** Noir complet. */
        DARK,
        /** Henry joue sa mélodie dans l'encadrement de la porte. */
        MELODY,
        /** Henry est entré. */
        ATTACKED
    }

    private final Dice dice;
    private Phase phase = Phase.WAITING;
    private double timer;

    /**
     * Crée Henry.
     *
     * @param dice source de hasard des durées
     */
    public Henry(Dice dice) {
        this.dice = Objects.requireNonNull(dice, "dice");
    }

    @Override
    public void update(double deltaSeconds, GameContext ctx) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        switch (phase) {
            case WAITING -> {
                if (ctx.power.isPowerOut()) {
                    phase = Phase.DARK;
                    timer = DARK_BASE + dice.rollD20() * SECONDS_PER_ROLL;
                }
            }
            case DARK -> {
                timer -= deltaSeconds;
                if (timer <= 0) {
                    phase = Phase.MELODY;
                    timer = MELODY_BASE + dice.rollD20() * SECONDS_PER_ROLL;
                    ctx.publish(new GameEvent.HenryMelody(true));
                }
            }
            case MELODY -> {
                timer -= deltaSeconds;
                if (timer <= 0) {
                    phase = Phase.ATTACKED;
                    ctx.publish(new GameEvent.HenryMelody(false));
                    ctx.attackPlayer(NAME);
                }
            }
            case ATTACKED -> {
                // Plus rien à faire : la nuit est perdue.
            }
        }
    }

    @Override
    public void reset() {
        phase = Phase.WAITING;
        timer = 0;
    }

    /**
     * Renvoie l'étape de la coupure de courant.
     *
     * @return étape en cours
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * Indique si Henry joue sa mélodie dans l'encadrement de la porte.
     *
     * @return {@code true} pendant la mélodie
     */
    public boolean isPlayingMelody() {
        return phase == Phase.MELODY;
    }
}
