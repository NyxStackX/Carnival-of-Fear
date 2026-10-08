package com.epitech.fnah.core;

import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * État de la nuit transmis aux éléments mis à jour : horloge, énergie, régie
 * et plan du chapiteau, plus le moyen de déclencher une attaque contre le joueur.
 */
public class GameContext {
    /** Horloge de la nuit. */
    public final GameClock clock;
    /** Réserve d'énergie. */
    public final PowerSystem power;
    /** Régie du joueur. */
    public final Office office;
    /** Plan du chapiteau. */
    public final MapGraph map;

    private final Consumer<String> attackHandler;

    /**
     * Regroupe l'état de la nuit.
     *
     * @param clock         horloge de la nuit
     * @param power         système d'énergie
     * @param office        régie du joueur
     * @param map           plan du chapiteau
     * @param attackHandler appelé avec le nom du clown quand il attaque
     */
    public GameContext(GameClock clock, PowerSystem power, Office office, MapGraph map,
                       Consumer<String> attackHandler) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.power = Objects.requireNonNull(power, "power");
        this.office = Objects.requireNonNull(office, "office");
        this.map = Objects.requireNonNull(map, "map");
        this.attackHandler = Objects.requireNonNull(attackHandler, "attackHandler");
    }

    /**
     * Signale qu'un clown est entré dans la régie.
     *
     * @param attackerName nom du clown
     */
    public void attackPlayer(String attackerName) {
        attackHandler.accept(attackerName);
    }
}
