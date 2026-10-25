package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.entity.Mimi;
import com.epitech.fnah.entity.ZigEtZag;
import com.epitech.fnah.model.Room;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Musiques et effets sonores, déclenchés par les événements de la partie.
 *
 * <p>Écoute l'{@link com.epitech.fnah.core.EventBus} : la logique du jeu ne
 * connaît pas le son. Les bruits venant de la gauche (porte gauche, Bobo) sont
 * placés à gauche dans le casque, et Bobo est plus fort à mesure qu'il approche.</p>
 *
 * <p>Fichiers lus dans {@code src/main/resources/audio/} : {@code menu} et
 * {@code nuit} ({@code .mp3} prioritaire s'il est lisible, sinon {@code .wav})
 * et les effets du dossier {@code sfx/}. Un fichier absent est ignoré sans erreur.</p>
 *
 * <p>Au lancement, la sortie audio de la machine est vérifiée. Si elle est
 * absente (cas fréquent sous WSL), le jeu le signale et continue sans son.</p>
 */
public final class SoundManager {

    private static final String[] EFFECTS = {"door_close", "door_open", "tablet_up", "tablet_down",
        "camera_switch", "static_loop", "klaxon", "footsteps", "scream", "chime_six", "hour_bell",
        "menu_move", "menu_select", "jack_pop", "henry_entry"};
    private static final double LEFT = -0.7;

    private final Game game;
    private final MediaPlayer menuMusic;
    private final MediaPlayer nightMusic;
    private final Map<String, AudioClip> effects = new HashMap<>();
    private final boolean available;
    private boolean muted;

    /**
     * Charge les sons et s'abonne aux événements de la partie.
     *
     * @param game partie en cours
     */
    public SoundManager(Game game) {
        this.game = game;
        this.available = audioOutputAvailable();
        if (!available) {
            System.err.println("Aucune sortie audio détectée : le jeu continue sans son.");
            System.err.println("Sous WSL : sudo apt install libasound2-plugins, puis voir la section Son du README.");
        }
        this.menuMusic = available ? loadMusic("menu", 0.6) : null;
        this.nightMusic = available ? loadMusic("nuit", 0.75) : null;
        for (String name : available ? EFFECTS : new String[0]) {
            URL url = SoundManager.class.getResource("/audio/sfx/" + name + ".wav");
            if (url != null) {
                try {
                    effects.put(name, new AudioClip(url.toExternalForm()));
                } catch (MediaException e) {
                    System.err.println("Effet sonore illisible : " + name + " (" + e.getMessage() + ")");
                }
            }
        }
        AudioClip hiss = effects.get("static_loop");
        if (hiss != null) {
            hiss.setCycleCount(AudioClip.INDEFINITE);
        }

        var events = game.getEvents();
        events.subscribe(GameEvent.StateChanged.class, e -> onState(e.state()));
        events.subscribe(GameEvent.DoorToggled.class, e -> play(e.closed() ? "door_close" : "door_open", 0.9,
                e.side() == com.epitech.fnah.model.Door.Side.LEFT ? LEFT : -LEFT));
        events.subscribe(GameEvent.TabletToggled.class, e -> onTablet(e.open()));
        events.subscribe(GameEvent.CameraSelected.class, e -> play("camera_switch", 0.5, 0));
        events.subscribe(GameEvent.AnimatronicMoved.class, this::onAnimatronicMoved);
        events.subscribe(GameEvent.FakeHornPlayed.class, e -> play("klaxon", 0.65, LEFT));
        events.subscribe(GameEvent.HourChanged.class, e -> {
            if (e.hour() < 6) {
                play("hour_bell", 0.45, 0);
            }
        });
        onState(game.getState());
    }

    /** Son de déplacement dans un menu. */
    public void menuMove() {
        play("menu_move", 0.35, 0);
    }

    /** Son de validation dans un menu. */
    public void menuSelect() {
        play("menu_select", 0.6, 0);
    }

    /** Diable qui jaillit de sa boîte (menu). */
    public void jackPop() {
        play("jack_pop", 0.7, 0.4);
    }

    /** Coupe ou rétablit tout le son. */
    public void toggleMute() {
        muted = !muted;
        if (menuMusic != null) {
            menuMusic.setMute(muted);
        }
        if (nightMusic != null) {
            nightMusic.setMute(muted);
        }
        if (muted) {
            stopClip("static_loop");
            stopClip("henry_entry");
        } else if (game.getState() == GameState.PLAYING && game.getOffice().getCameras().isOpen()) {
            startHiss();
        }
    }

    /**
     * Indique si une sortie audio a été trouvée au lancement.
     *
     * @return {@code false} si la machine n'a aucune sortie audio utilisable
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Indique si le son est coupé.
     *
     * @return {@code true} si le son est coupé
     */
    public boolean isMuted() {
        return muted;
    }

    boolean isHenryEntryAvailable() {
        return available && !muted;
    }

    private void onState(GameState state) {
        switch (state) {
            case MENU, NIGHT_INTRO -> {
                stop(nightMusic);
                stopClip("static_loop");
                play(menuMusic);
            }
            case PLAYING -> {
                stop(menuMusic);
                play(nightMusic);
            }
            case JUMPSCARE -> {
                stop(menuMusic);
                stop(nightMusic);
                stopClip("static_loop");
                play("scream", 0.95, 0);
            }
            case WIN -> {
                stop(nightMusic);
                stopClip("static_loop");
                play("chime_six", 0.8, 0);
            }
            case GAME_OVER -> {
                stop(nightMusic);
                stopClip("static_loop");
                if (game.getPower().isPowerOut()) {
                    play("henry_entry", 0.7, 0);
                }
            }
        }
    }

    private void onTablet(boolean open) {
        if (open) {
            play("tablet_up", 0.6, 0);
            startHiss();
        } else {
            play("tablet_down", 0.6, 0);
            stopClip("static_loop");
        }
    }

    private void onAnimatronicMoved(GameEvent.AnimatronicMoved event) {
        if (Mimi.NAME.equals(event.name())) {
            play("footsteps", 0.55, -LEFT);
            return;
        }
        if (ZigEtZag.NAME.equals(event.name())) {
            return;
        }
        if (event.to() == Room.REGIE) {
            return;
        }
        int before = game.getMap().distance(event.from(), Room.REGIE);
        int after = game.getMap().distance(event.to(), Room.REGIE);
        if (after < before) {
            double volume = Math.max(0.25, 1.0 - 0.22 * (after - 1));
            play("klaxon", volume, after == 1 ? -0.85 : -0.4);
        } else {
            play("footsteps", 0.6, -0.4);
        }
    }

    private void startHiss() {
        AudioClip hiss = effects.get("static_loop");
        if (!muted && hiss != null && !hiss.isPlaying()) {
            hiss.play(0.12);
        }
    }

    private void play(String effect, double volume, double balance) {
        AudioClip clip = effects.get(effect);
        if (!muted && clip != null) {
            clip.play(volume, balance, 1.0, 0.0, 1);
        }
    }

    private void stopClip(String effect) {
        AudioClip clip = effects.get(effect);
        if (clip != null) {
            clip.stop();
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

    /**
     * Vérifie qu'une sortie audio s'ouvre réellement. Sous Linux et WSL, JavaFX et
     * Java Sound passent tous deux par ALSA : si cette ligne ne s'ouvre pas, aucun
     * son ne sortira.
     */
    private static boolean audioOutputAvailable() {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            return true;
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            return false;
        }
    }

    private static MediaPlayer loadMusic(String name, double volume) {
        for (String extension : new String[]{".mp3", ".wav"}) {
            URL url = SoundManager.class.getResource("/audio/" + name + extension);
            if (url == null) {
                continue;
            }
            try {
                MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
                player.setCycleCount(MediaPlayer.INDEFINITE);
                player.setVolume(volume);
                player.setOnError(() -> System.err.println("Lecture impossible de " + name + extension
                        + " : " + player.getError()));
                return player;
            } catch (MediaException e) {
                System.err.println("Lecture audio impossible pour " + name + extension + " : " + e.getMessage());
            }
        }
        System.err.println("Musique " + name + " indisponible : le jeu continue sans elle.");
        return null;
    }
}
