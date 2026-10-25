package com.epitech.fnah.view;

import com.epitech.fnah.core.EventBus;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * Musiques du jeu : une pour le menu, une pour l'ambiance des nuits.
 *
 * <p>Écoute les changements d'état sur l'{@link EventBus} : la musique du menu
 * joue au menu, l'ambiance pendant la nuit, et tout se coupe au jumpscare.
 * Les fichiers sont lus dans {@code src/main/resources/audio/} :
 * {@code menu.mp3} et {@code nuit.mp3} (ou {@code .wav}). Un fichier absent
 * ou illisible n'empêche pas de jouer : le jeu reste simplement silencieux.</p>
 */
public final class AudioManager {

    private static final String FOLDER = "/audio/";
    private static final String[] EXTENSIONS = {".mp3", ".wav"};
    private static final double MENU_VOLUME = 0.6;
    private static final double NIGHT_VOLUME = 0.35;

    private final MediaPlayer menuMusic;
    private final MediaPlayer nightMusic;
    private boolean muted;

    /**
     * Charge les musiques et les synchronise avec l'état du jeu.
     *
     * @param events  canal d'événements de la partie
     * @param initial état du jeu au lancement (en général le menu)
     */
    public AudioManager(EventBus events, GameState initial) {
        this.menuMusic = loadLoop("menu", MENU_VOLUME);
        this.nightMusic = loadLoop("nuit", NIGHT_VOLUME);
        events.subscribe(GameEvent.StateChanged.class, e -> onState(e.state()));
        onState(initial);
    }

    /** Coupe ou rétablit le son. */
    public void toggleMute() {
        muted = !muted;
        if (menuMusic != null) {
            menuMusic.setMute(muted);
        }
        if (nightMusic != null) {
            nightMusic.setMute(muted);
        }
    }

    /**
     * Indique si le son est coupé.
     *
     * @return {@code true} si le son est coupé
     */
    public boolean isMuted() {
        return muted;
    }

    private void onState(GameState state) {
        switch (state) {
            case MENU -> {
                stop(nightMusic);
                play(menuMusic);
            }
            case PLAYING -> {
                stop(menuMusic);
                play(nightMusic);
            }
            default -> {
                stop(menuMusic);
                stop(nightMusic);
            }
        }
    }

    private static void play(MediaPlayer player) {
        if (player != null && player.getStatus() != MediaPlayer.Status.PLAYING) {
            player.play();
        }
    }

    private static void stop(MediaPlayer player) {
        if (player != null) {
            player.stop();
        }
    }

    private static MediaPlayer loadLoop(String name, double volume) {
        for (String extension : EXTENSIONS) {
            URL url = AudioManager.class.getResource(FOLDER + name + extension);
            if (url == null) {
                continue;
            }
            try {
                MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
                player.setCycleCount(MediaPlayer.INDEFINITE);
                player.setVolume(volume);
                player.setOnError(() -> System.err.println(
                        "Lecture impossible de " + name + extension + " : " + player.getError()));
                return player;
            } catch (MediaException e) {
                System.err.println("Format non pris en charge pour " + name + extension + " : " + e.getMessage());
            }
        }
        System.err.println("Aucune musique « " + name + " » dans " + FOLDER + " : le jeu reste silencieux.");
        return null;
    }
}
