package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptionListTest {

    @Test
    void activationRunsItsActionEvenWhenSelectionSoundFails() {
        Game game = new Game(() -> 20);
        OptionList options = new OptionList(120, 430, 52, 470, () -> { },
                () -> {
                    throw new IllegalStateException("audio playback failed");
                })
                .add(() -> "JOUER", () -> game.startNight(1));

        assertThrows(IllegalStateException.class, () -> options.onClick(120, 430));

        assertEquals(GameState.PLAYING, game.getState());
    }
}
