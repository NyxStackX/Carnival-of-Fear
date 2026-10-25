package com.epitech.fnah.core;

import com.epitech.fnah.config.Achievement;
import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.config.NightConfig;

import java.util.Objects;

/**
 * Surveille la nuit en cours sur l'{@link EventBus} et attribue les étoiles
 * (patron Observer) : elle note si une porte a été fermée ou la tablette levée,
 * puis vérifie les conditions à 06:00.
 *
 * <p>Chaque nouvelle étoile est ajoutée à la progression et annoncée par un
 * {@link GameEvent.AchievementUnlocked}. L'enregistrement du fichier reste à
 * la charge de l'application.</p>
 */
public final class AchievementTracker {

    private final EventBus events;
    private final GameConfig config;

    private int night;
    private boolean custom;
    private boolean maxChallenge;
    private boolean doorClosed;
    private boolean camerasUsed;

    /**
     * Crée le suivi et s'abonne aux événements de la partie.
     *
     * @param events canal d'événements de la partie
     * @param config progression où ranger les étoiles
     */
    public AchievementTracker(EventBus events, GameConfig config) {
        this.events = Objects.requireNonNull(events, "events");
        this.config = Objects.requireNonNull(config, "config");
        events.subscribe(GameEvent.NightStarted.class, this::onNightStarted);
        events.subscribe(GameEvent.DoorToggled.class, e -> doorClosed |= e.closed());
        events.subscribe(GameEvent.TabletToggled.class, e -> camerasUsed |= e.open());
        events.subscribe(GameEvent.StateChanged.class, e -> {
            if (e.state() == GameState.WIN) {
                onNightWon();
            }
        });
    }

    private void onNightStarted(GameEvent.NightStarted event) {
        night = event.night();
        custom = event.custom();
        maxChallenge = event.maxChallenge();
        doorClosed = false;
        camerasUsed = false;
    }

    private void onNightWon() {
        if (!custom && night == NightConfig.LAST_NIGHT) {
            unlock(Achievement.LAST_SHOW);
        }
        if (!doorClosed) {
            unlock(Achievement.OPEN_DOORS);
        }
        if (!camerasUsed) {
            unlock(Achievement.EYES_CLOSED);
        }
        if (custom && maxChallenge) {
            unlock(Achievement.MAX_CHALLENGE);
        }
    }

    private void unlock(Achievement achievement) {
        if (config.unlock(achievement)) {
            events.publish(new GameEvent.AchievementUnlocked(achievement));
        }
    }
}
