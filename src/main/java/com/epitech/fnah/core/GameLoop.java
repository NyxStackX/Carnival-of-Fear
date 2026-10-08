package com.epitech.fnah.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameLoop {

    private final GameClock clock;
    private final com.epitech.fnah.model.PowerSystem power;
    private final GameContext context;
    private final List<Updatable> systems = new ArrayList<>();

    private GameState state = GameState.MENU;
    private final List<Consumer<GameState>> stateListeners = new ArrayList<>();

    public GameLoop(GameClock clock, com.epitech.fnah.model.PowerSystem power) {
        this.clock = clock;
        this.power = power;
        this.context = new GameContext(clock, power);
        power.onPowerOut(p -> setState(GameState.GAME_OVER));
    }

    public void registerSystem(Updatable system) {
        systems.add(system);
    }

    public void startNight() {
        clock.reset();
        power.reset();
        setState(GameState.PLAYING);
    }

    public void tick(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        if (state != GameState.PLAYING) {
            return;
        }

        clock.update(deltaSeconds);
        power.update(deltaSeconds);

        for (Updatable s : systems) {
            s.update(deltaSeconds, context);
        }

        if (clock.isNightOver() && state == GameState.PLAYING) {
            setState(GameState.WIN);
        }
    }

    public GameState getState() { return state; }

    public GameContext getContext() { return context; }

    public void setState(GameState newState) {
        if (this.state == newState) return;
        this.state = newState;
        for (Consumer<GameState> l : stateListeners) {
            l.accept(newState);
        }
    }

    public void onStateChange(Consumer<GameState> listener) {
        stateListeners.add(listener);
    }
}