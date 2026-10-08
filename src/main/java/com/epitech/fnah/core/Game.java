package com.epitech.fnah.core;

import com.epitech.fnah.config.NightConfig;
import com.epitech.fnah.entity.Bobo;
import com.epitech.fnah.entity.Dice;
import com.epitech.fnah.entity.RandomDice;
import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;

/**
 * Point d'entrée de la logique du jeu pour l'affichage (patron Facade).
 *
 * <p>Crée et relie l'horloge, l'énergie, la régie, le plan, Bobo et la boucle,
 * gère l'enchaînement des nuits, et publie ce qui se passe sur l'{@link EventBus}.
 * L'affichage n'a besoin que de cette classe.</p>
 */
public class Game {

    private final GameClock clock = new GameClock();
    private final PowerSystem power = new PowerSystem();
    private final Office office = new Office();
    private final MapGraph map = MapGraph.circus();
    private final EventBus events = new EventBus();
    private final Bobo bobo;
    private final GameLoop loop;

    private int currentNight = 1;

    /** Crée une partie avec un vrai hasard. */
    public Game() {
        this(new RandomDice());
    }

    /**
     * Crée une partie avec une source de hasard donnée (utile pour les tests).
     *
     * @param dice source de hasard des clowns
     */
    public Game(Dice dice) {
        this.bobo = new Bobo(0, dice);
        this.loop = new GameLoop(clock, power, office, map);
        loop.registerSystem(bobo);

        loop.onStateChange(state -> events.publish(new GameEvent.StateChanged(state)));
        bobo.onMove((from, to) -> events.publish(new GameEvent.AnimatronicMoved(bobo.getName(), from, to)));
        office.getLeftDoor().onToggle(door -> events.publish(new GameEvent.DoorToggled(door.getSide(), door.isClosed())));
        clock.onHourChange(hour -> events.publish(new GameEvent.HourChanged(hour)));
    }

    /**
     * Choisit la nuit à lancer depuis le menu, sans la démarrer.
     *
     * @param night numéro de la nuit (1 à {@link NightConfig#LAST_NIGHT})
     */
    public void selectNight(int night) {
        NightConfig.forNight(night);
        currentNight = night;
    }

    /**
     * Lance une nuit avec ses réglages.
     *
     * @param night numéro de la nuit (1 à {@link NightConfig#LAST_NIGHT})
     */
    public void startNight(int night) {
        NightConfig config = NightConfig.forNight(night);
        currentNight = night;
        bobo.setBaseAiLevel(config.boboAiLevel());
        power.setEnergyMultiplier(config.energyMultiplier());
        loop.startNight();
    }

    /** Relance la nuit sélectionnée depuis le début. */
    public void restartNight() {
        startNight(currentNight);
    }

    /**
     * Renvoie {@code true} s'il reste une nuit après la nuit en cours.
     *
     * @return {@code true} s'il reste une nuit après la nuit en cours
     */
    public boolean hasNextNight() {
        return currentNight < NightConfig.LAST_NIGHT;
    }

    /**
     * Lance la nuit suivante.
     *
     * @throws IllegalStateException si la dernière nuit est déjà atteinte
     */
    public void startNextNight() {
        if (!hasNextNight()) {
            throw new IllegalStateException("no night after " + currentNight);
        }
        startNight(currentNight + 1);
    }

    /** Revient au menu principal. */
    public void returnToMenu() {
        loop.setState(GameState.MENU);
    }

    /** Ouvre ou ferme la porte gauche. Sans effet si aucune nuit n'est en cours. */
    public void toggleLeftDoor() {
        if (loop.getState() == GameState.PLAYING) {
            office.getLeftDoor().toggle();
        }
    }

    /**
     * Fait avancer la partie d'un tick.
     *
     * @param deltaSeconds temps écoulé depuis le tick précédent, en secondes
     */
    public void tick(double deltaSeconds) {
        loop.tick(deltaSeconds);
    }

    /**
     * Renvoie le canal d'événements auquel l'affichage s'abonne.
     *
     * @return canal d'événements auquel l'affichage s'abonne
     */
    public EventBus getEvents() { return events; }

    /**
     * Renvoie l'état courant.
     *
     * @return état courant
     */
    public GameState getState() { return loop.getState(); }

    /**
     * Renvoie le numéro de la nuit en cours ou sélectionnée.
     *
     * @return numéro de la nuit en cours ou sélectionnée
     */
    public int getCurrentNight() { return currentNight; }

    /**
     * Renvoie le nom du clown qui a attaqué, ou {@code null}.
     *
     * @return nom du clown qui a attaqué, ou {@code null}
     */
    public String getAttackerName() { return loop.getAttackerName(); }

    /**
     * Renvoie l'horloge de la nuit.
     *
     * @return horloge
     */
    public GameClock getClock() { return clock; }

    /**
     * Renvoie le système d'énergie.
     *
     * @return système d'énergie
     */
    public PowerSystem getPower() { return power; }

    /**
     * Renvoie la régie du joueur.
     *
     * @return régie
     */
    public Office getOffice() { return office; }

    /**
     * Renvoie le plan du chapiteau.
     *
     * @return plan du chapiteau
     */
    public MapGraph getMap() { return map; }

    /**
     * Renvoie Bobo, le seul clown actif pour l'instant.
     *
     * @return Bobo
     */
    public Bobo getBobo() { return bobo; }
}
