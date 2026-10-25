package com.epitech.fnah.core;

import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Boucle logique d'une nuit : fait avancer l'horloge, l'énergie et les éléments
 * enregistrés, et gère les changements d'état (victoire, jumpscare, défaite).
 *
 * <p>Ordre d'un tick : horloge, énergie (panne = défaite), 06:00 (victoire),
 * puis clowns. Une victoire à 06:00 passe donc avant une attaque du même tick.</p>
 */
public final class GameLoop {

    /** Durée du jumpscare avant l'écran de défaite, en secondes. */
    public static final double JUMPSCARE_DURATION = 1.5;

    private final GameClock clock;
    private final PowerSystem power;
    private final Office office;
    private final GameContext context;
    private final List<Updatable> systems = new ArrayList<>();

    private GameState state = GameState.MENU;
    private final List<Consumer<GameState>> stateListeners = new ArrayList<>();

    private double jumpscareTimer;
    private String attackerName;

    /**
     * Crée une boucle avec une régie neuve et le plan du Cirque Malvaux.
     *
     * @param clock horloge de la nuit
     * @param power système d'énergie
     */
    public GameLoop(GameClock clock, PowerSystem power) {
        this(clock, power, new Office(), MapGraph.circus());
    }

    /**
     * Crée une boucle avec une régie et un plan donnés.
     *
     * @param clock  horloge de la nuit
     * @param power  système d'énergie
     * @param office régie du joueur, branchée sur le système d'énergie
     * @param map    plan du chapiteau
     */
    public GameLoop(GameClock clock, PowerSystem power, Office office, MapGraph map) {
        this(clock, power, office, map, event -> { });
    }

    /**
     * Crée une boucle qui transfère aussi les signaux des clowns au canal d'événements.
     *
     * @param clock horloge de la nuit
     * @param power système d'énergie
     * @param office régie
     * @param map plan du chapiteau
     * @param eventPublisher diffuseur d'événements
     */
    public GameLoop(GameClock clock, PowerSystem power, Office office, MapGraph map,
                    Consumer<GameEvent> eventPublisher) {
        this.clock = clock;
        this.power = power;
        this.office = office;
        this.context = new GameContext(clock, power, office, map, this::triggerJumpscare, eventPublisher);
        power.addConsumer(office);
        power.onPowerOut(p -> setState(GameState.GAME_OVER));
    }

    /**
     * Ajoute un élément mis à jour à chaque tick (un clown, une mécanique...).
     *
     * @param system élément à mettre à jour à chaque tick pendant la nuit
     */
    public void registerSystem(Updatable system) {
        systems.add(system);
    }

    /** Remet tout à zéro et lance une nuit. */
    public void startNight() {
        clock.reset();
        power.reset();
        office.reset();
        for (Updatable s : systems) {
            s.reset();
        }
        jumpscareTimer = 0;
        attackerName = null;
        setState(GameState.PLAYING);
    }

    /** Met la nuit en pause si elle est en cours. */
    public void pause() {
        if (state == GameState.PLAYING) {
            setState(GameState.PAUSED);
        }
    }

    /** Reprend une nuit mise en pause. */
    public void resume() {
        if (state == GameState.PAUSED) {
            setState(GameState.PLAYING);
        }
    }

    /**
     * Fait avancer le jeu.
     *
     * @param deltaSeconds temps écoulé depuis le tick précédent, en secondes
     * @throws IllegalArgumentException si {@code deltaSeconds} est négatif
     */
    public void tick(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        if (state == GameState.JUMPSCARE) {
            jumpscareTimer += deltaSeconds;
            if (jumpscareTimer >= JUMPSCARE_DURATION) {
                setState(GameState.GAME_OVER);
            }
            return;
        }
        if (state != GameState.PLAYING) {
            return;
        }

        clock.update(deltaSeconds);
        power.update(deltaSeconds);
        office.getCameras().update(deltaSeconds);
        if (state != GameState.PLAYING) {
            return;
        }
        if (clock.isNightOver()) {
            setState(GameState.WIN);
            return;
        }

        for (Updatable s : systems) {
            s.update(deltaSeconds, context);
            if (state != GameState.PLAYING) {
                return;
            }
        }
    }

    /**
     * Lance le jumpscare. Ignoré si la nuit n'est pas en cours.
     *
     * @param attacker nom du clown qui attaque
     */
    public void triggerJumpscare(String attacker) {
        if (state != GameState.PLAYING) {
            return;
        }
        attackerName = attacker;
        jumpscareTimer = 0;
        setState(GameState.JUMPSCARE);
    }


    /**
     * Renvoie l'état courant du jeu.
     *
     * @return état courant du jeu
     */
    public GameState getState() { return state; }

    /**
     * Renvoie le contexte partagé avec les éléments mis à jour.
     *
     * @return contexte partagé avec les éléments mis à jour
     */
    public GameContext getContext() { return context; }

    /**
     * Renvoie le nom du clown qui a attaqué, ou {@code null} (panne ou nuit en cours).
     *
     * @return nom du clown qui a attaqué, ou {@code null} (panne ou nuit en cours)
     */
    public String getAttackerName() { return attackerName; }

    /**
     * Change l'état du jeu.
     *
     * @param newState nouvel état ; les écouteurs ne sont prévenus que s'il change
     */
    public void setState(GameState newState) {
        if (this.state == newState) return;
        this.state = newState;
        for (Consumer<GameState> l : stateListeners) {
            l.accept(newState);
        }
    }

    /**
     * Enregistre un écouteur de changement d'état.
     *
     * @param listener appelé à chaque changement d'état
     */
    public void onStateChange(Consumer<GameState> listener) {
        stateListeners.add(listener);
    }
}
