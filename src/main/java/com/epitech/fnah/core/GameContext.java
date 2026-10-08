package com.epitech.fnah.core;

import com.epitech.fnah.model.PowerSystem;

public class GameContext {
    public final GameClock clock;
    public final PowerSystem power;

    public GameContext(GameClock clock, PowerSystem power) {
        this.clock = clock;
        this.power = power;
    }
}