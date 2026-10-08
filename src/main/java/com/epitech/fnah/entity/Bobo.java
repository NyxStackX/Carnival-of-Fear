package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.Room;

/**
 * Bobo, le clown triste. Il cherche à atteindre la porte gauche.
 *
 * <ul>
 *   <li>À chaque action réussie, il avance d'une salle sur le plus court chemin
 *       vers le couloir gauche (Piste centrale, Gradins, Coulisses ouest, Couloir gauche).</li>
 *   <li>Dans le couloir gauche, à sa prochaine action réussie :
 *       si la porte gauche est fermée, il se replie dans les Gradins ;
 *       sinon, il entre dans la régie et attaque.</li>
 * </ul>
 */
public class Bobo extends Animatronic {

    /** Nom affiché. */
    public static final String NAME = "Bobo";
    /** Secondes entre deux tentatives d'action. */
    public static final double ACTION_INTERVAL = 5.0;
    /** Salle de départ. */
    public static final Room START_ROOM = Room.PISTE_CENTRALE;
    /** Salle de repli quand la porte le bloque. */
    public static final Room RETREAT_ROOM = Room.GRADINS;
    /** Porte que Bobo attaque. */
    public static final Door.Side TARGET_SIDE = Door.Side.LEFT;

    /**
     * Crée Bobo sur la piste centrale.
     *
     * @param baseAiLevel niveau d'IA de départ (0 à 20)
     * @param dice        source de hasard
     */
    public Bobo(int baseAiLevel, Dice dice) {
        super(NAME, START_ROOM, baseAiLevel, ACTION_INTERVAL, dice);
    }

    @Override
    protected void act(GameContext ctx) {
        Room room = getRoom();
        if (room == Room.REGIE) {
            return;
        }
        if (room == TARGET_SIDE.getCorridor()) {
            if (ctx.office.getLeftDoor().isClosed()) {
                moveTo(RETREAT_ROOM);
            } else {
                moveTo(Room.REGIE);
                ctx.attackPlayer(getName());
            }
            return;
        }
        ctx.map.nextStepTowards(room, TARGET_SIDE.getCorridor()).ifPresent(this::moveTo);
    }

    /**
     * Renvoie {@code true} si Bobo attend derrière la porte gauche.
     *
     * @return {@code true} si Bobo attend derrière la porte gauche
     */
    public boolean isAtDoor() {
        return getRoom() == TARGET_SIDE.getCorridor();
    }
}
