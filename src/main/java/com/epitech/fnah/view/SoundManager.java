package com.epitech.fnah.view;

import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.entity.Henry;
import com.epitech.fnah.entity.Mimi;
import com.epitech.fnah.entity.Pepin;
import com.epitech.fnah.entity.ZigEtZag;
import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.Office;
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
import java.util.Set;

/**
 * Musiques, ambiances et effets sonores, déclenchés par les événements de la partie.
 *
 * <p>Écoute l'{@link com.epitech.fnah.core.EventBus} : la logique du jeu ne
 * connaît pas le son. Les bruits venant de la gauche (porte gauche, Bobo) sont
 * placés à gauche dans le casque, ceux de droite (Mimi) à droite, et Bobo est
 * plus fort à mesure qu'il approche.</p>
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
        "menu_move", "menu_select", "jack_pop", "henry_entry", "heartbeat", "rain_loop", "thunder",
        "whisper", "child_laugh", "typewriter", "tape_hiss", "breaker", "light_buzz", "organ_loop",
        "crank", "henry_melody", "crash_buzz", "honk"};
    /** Effets joués en boucle tant qu'on ne les arrête pas. */
    private static final Set<String> LOOPS = Set.of("static_loop", "rain_loop", "tape_hiss", "light_buzz",
            "organ_loop", "crank", "henry_melody");
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
                    AudioClip clip = new AudioClip(url.toExternalForm());
                    if (LOOPS.contains(name)) {
                        clip.setCycleCount(AudioClip.INDEFINITE);
                    }
                    effects.put(name, clip);
                } catch (MediaException e) {
                    System.err.println("Effet sonore illisible : " + name + " (" + e.getMessage() + ")");
                }
            }
        }

        var events = game.getEvents();
        events.subscribe(GameEvent.StateChanged.class, e -> onState(e.state()));
        events.subscribe(GameEvent.DoorToggled.class, e -> play(e.closed() ? "door_close" : "door_open", 0.9,
                e.side() == Door.Side.LEFT ? LEFT : -LEFT));
        events.subscribe(GameEvent.TabletToggled.class, e -> onTablet(e.open()));
        events.subscribe(GameEvent.CameraSelected.class, e -> {
            play("camera_switch", 0.5, 0);
            refreshOrgan();
        });
        events.subscribe(GameEvent.AnimatronicMoved.class, this::onAnimatronicMoved);
        events.subscribe(GameEvent.FakeHornPlayed.class, e -> play("klaxon", 0.65, LEFT));
        events.subscribe(GameEvent.HourChanged.class, e -> {
            if (e.hour() < 6) {
                play("hour_bell", 0.45, 0);
            }
        });
        events.subscribe(GameEvent.LightToggled.class, e -> {
            play("camera_switch", 0.3, e.side() == Door.Side.LEFT ? LEFT : -LEFT);
            refreshLights();
        });
        events.subscribe(GameEvent.PowerOut.class, e -> onPowerOut());
        events.subscribe(GameEvent.HenryMelody.class, e -> {
            if (e.playing()) {
                startLoop("henry_melody", 0.7);
            } else {
                stopClip("henry_melody");
            }
        });
        events.subscribe(GameEvent.OrganWinding.class, e -> {
            if (e.winding()) {
                startLoop("crank", 0.5);
            } else {
                stopClip("crank");
            }
        });
        events.subscribe(GameEvent.PepinEscaped.class, e -> {
            play("jack_pop", e.out() ? 0.9 : 0.5, 0);
            if (e.out()) {
                play("child_laugh", 0.7, 0.2);
            }
            refreshOrgan();
        });
        events.subscribe(GameEvent.AchievementUnlocked.class, e -> play("chime_six", 0.5, 0));
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

    /** Un battement de cœur (un clown est tout proche). */
    public void heartbeat() {
        play("heartbeat", 0.85, 0);
    }

    /** Une frappe de machine à écrire (texte de la cassette). */
    public void typewriter() {
        play("typewriter", 0.25, 0);
    }

    /**
     * Lance ou arrête le souffle de la cassette.
     *
     * @param on {@code true} pendant la lecture de la cassette
     */
    public void tape(boolean on) {
        if (on) {
            startLoop("tape_hiss", 0.35);
        } else {
            stopClip("tape_hiss");
        }
    }

    /** Coup de tonnerre (orage). */
    public void thunder() {
        play("thunder", 0.9, Draw.random(-0.4, 0.4));
    }

    /** Chuchotement tout près (hallucination). */
    public void whisper() {
        play("whisper", 0.7, Draw.random(-0.8, 0.8));
    }

    /** Rire d'enfant lointain (hallucination). */
    public void laugh() {
        play("child_laugh", 0.5, Draw.random(-0.8, 0.8));
    }

    /** « Pouet » du nez de clown (easter egg). */
    public void honk() {
        play("honk", 0.8, 0);
    }

    /** Faux plantage : tout se coupe, il ne reste qu'un bourdonnement. */
    public void crash() {
        if (nightMusic != null) {
            nightMusic.pause();
        }
        stopLoops();
        play("crash_buzz", 0.5, 0);
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
            stopLoops();
        } else if (game.getState() == GameState.PLAYING) {
            resumeLoops();
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

    private void onState(GameState state) {
        switch (state) {
            case MENU, NIGHT_INTRO, ENDING -> {
                stop(nightMusic);
                stopLoops();
                play(menuMusic);
            }
            case PLAYING -> {
                stop(menuMusic);
                if (!game.getPower().isPowerOut()) {
                    play(nightMusic);
                }
                resumeLoops();
            }
            case PAUSED -> {
                if (nightMusic != null) {
                    nightMusic.pause();
                }
                stopLoops();
            }
            case JUMPSCARE -> {
                stop(menuMusic);
                stop(nightMusic);
                stopLoops();
                play("scream", 0.95, 0);
            }
            case WIN -> {
                stop(nightMusic);
                stopLoops();
                play("chime_six", 0.8, 0);
            }
            case GAME_OVER -> {
                stop(nightMusic);
                stopLoops();
            }
        }
    }

    private void onPowerOut() {
        stop(nightMusic);
        stopClip("static_loop");
        stopClip("light_buzz");
        stopClip("organ_loop");
        stopClip("crank");
        play("breaker", 0.9, 0);
    }

    private void onTablet(boolean open) {
        if (open) {
            play("tablet_up", 0.6, 0);
            startLoop("static_loop", 0.12);
        } else {
            play("tablet_down", 0.6, 0);
            stopClip("static_loop");
        }
        refreshOrgan();
    }

    private void onAnimatronicMoved(GameEvent.AnimatronicMoved event) {
        String name = event.name();
        if (Mimi.NAME.equals(name)) {
            play("footsteps", 0.55, -LEFT);
            return;
        }
        if (ZigEtZag.NAME.equals(name) || Pepin.NAME.equals(name) || Henry.NAME.equals(name)) {
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

    /** Relance les boucles d'ambiance qui correspondent à l'état de la nuit. */
    private void resumeLoops() {
        startLoop("rain_loop", 0.22);
        if (game.getHenry().isPlayingMelody()) {
            startLoop("henry_melody", 0.7);
        }
        if (game.getOffice().getCameras().isOpen()) {
            startLoop("static_loop", 0.12);
        }
        refreshLights();
        refreshOrgan();
    }

    private void refreshLights() {
        Office office = game.getOffice();
        if (office.getLeftLight().isOn() || office.getRightLight().isOn()) {
            startLoop("light_buzz", 0.18);
        } else {
            stopClip("light_buzz");
        }
    }

    /** L'orgue de barbarie s'entend quand on regarde la caméra 8 et qu'il joue encore. */
    private void refreshOrgan() {
        Office office = game.getOffice();
        boolean audible = game.getState() == GameState.PLAYING && !game.getPower().isPowerOut()
                && office.getCameras().isWatching(Pepin.START_ROOM) && !office.getOrgan().isEmpty();
        if (audible) {
            startLoop("organ_loop", 0.45);
        } else {
            stopClip("organ_loop");
        }
    }

    private void stopLoops() {
        for (String loop : LOOPS) {
            stopClip(loop);
        }
    }

    private void startLoop(String effect, double volume) {
        AudioClip clip = effects.get(effect);
        if (!muted && clip != null && !clip.isPlaying()) {
            clip.play(volume);
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
