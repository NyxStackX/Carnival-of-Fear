package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.Room;

import java.util.List;

/**
 * Bobo, le clown triste. Il chasse le joueur jusqu'à la porte gauche.
 *
 * <ul>
 *   <li><b>Traque</b> : à chaque action réussie, il avance d'une salle sur le plus
 *       court chemin vers le couloir gauche. Le bruit fait dans la régie le
 *       rend plus actif et plus déterminé.</li>
 *   <li><b>Ronde</b> : parfois, il erre dans une salle voisine au lieu d'avancer,
 *       ce qui le rend moins prévisible (moins souvent quand il entend du bruit).</li>
 *   <li><b>À la porte</b> : arrivé dans le couloir gauche, il attend au plus
 *       {@link #lurkTime(int)} secondes. Si la porte est encore ouverte à ce
 *       moment-là (ou lors d'une action réussie avant), il entre et attaque :
 *       un clown devant une porte ouverte tue toujours. Si elle est fermée,
 *       il se replie dans les Gradins.</li>
 *   <li><b>Recherche</b> : après avoir été repoussé, il a perdu la trace du
 *       joueur et erre pendant {@value #SEARCH_MOVES} actions avant de reprendre la traque.</li>
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
    /** Nombre d'actions passées à errer après avoir été repoussé. */
    public static final int SEARCH_MOVES = 2;
    /** Jet de dé (sur 20) sous lequel il traque au lieu d'errer, sans bonus. */
    public static final int HUNT_THRESHOLD = 13;

    private double lurkTimer;
    private int searchMoves;

    /**
     * Crée Bobo sur la piste centrale.
     *
     * @param baseAiLevel niveau d'IA de départ
     * @param dice        source de hasard
     */
    public Bobo(int baseAiLevel, Dice dice) {
        super(NAME, START_ROOM, baseAiLevel, ACTION_INTERVAL, dice);
    }

    /**
     * Temps maximal que Bobo passe derrière la porte avant d'agir, selon son niveau.
     *
     * @param level niveau d'IA
     * @return secondes d'attente, entre 2 et 5
     */
    public static double lurkTime(int level) {
        return Math.max(2.0, 5.0 - 0.15 * level);
    }

    @Override
    protected boolean updateSpecialBehavior(double deltaSeconds, GameContext ctx) {
        if (!isAtDoor() || getAiLevel(ctx.clock.getHour()) == 0) {
            return false;
        }
        lurkTimer += deltaSeconds;
        if (lurkTimer >= lurkTime(getAiLevel(ctx.clock.getHour()))) {
            resolveAtDoor(ctx);
            return true;
        }
        return false;
    }

    @Override
    protected int hearingBonus(GameContext ctx) {
        return ctx.office.getNoise().hearingBonus();
    }

    @Override
    protected void act(GameContext ctx) {
        Room room = getRoom();
        if (room == Room.REGIE) {
            return;
        }
        if (isAtDoor()) {
            resolveAtDoor(ctx);
            return;
        }
        if (searchMoves > 0) {
            searchMoves--;
            wander(ctx);
        } else if (rollD20() <= HUNT_THRESHOLD + getBaseAiLevel() / 2 + 2 * hearingBonus(ctx)) {
            ctx.map.nextStepTowards(room, TARGET_SIDE.getCorridor()).ifPresent(this::moveTo);
        } else {
            wander(ctx);
        }
        if (isAtDoor()) {
            lurkTimer = 0;
        }
    }

    /** Erre vers une salle voisine (jamais directement dans la régie). */
    private void wander(GameContext ctx) {
        List<Room> next = ctx.map.neighbors(getRoom()).stream().filter(r -> r != Room.REGIE).toList();
        if (!next.isEmpty()) {
            moveTo(next.get(Math.floorMod(rollD20(), next.size())));
        }
    }

    /** Décide derrière la porte : entrer si elle est ouverte, sinon se replier et chercher ailleurs. */
    private void resolveAtDoor(GameContext ctx) {
        lurkTimer = 0;
        if (ctx.office.getLeftDoor().isClosed()) {
            moveTo(RETREAT_ROOM);
            searchMoves = SEARCH_MOVES;
        } else {
            moveTo(Room.REGIE);
            ctx.attackPlayer(getName());
        }
    }

    @Override
    protected void resetSpecificBehavior() {
        lurkTimer = 0;
        searchMoves = 0;
    }

    /**
     * Indique si Bobo est juste derrière la porte gauche.
     *
     * @return {@code true} s'il est dans le couloir gauche
     */
    public boolean isAtDoor() {
        return getRoom() == TARGET_SIDE.getCorridor();
    }

    /**
     * Indique si Bobo a perdu la trace du joueur et erre.
     *
     * @return {@code true} pendant sa recherche
     */
    public boolean isSearching() {
        return searchMoves > 0;
    }
}
