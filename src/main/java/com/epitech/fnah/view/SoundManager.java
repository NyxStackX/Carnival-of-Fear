package com.epitech.fnah.view;

import com.epitech.fnah.config.GameConfig;
import com.epitech.fnah.core.Game;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.core.GameState;
import com.epitech.fnah.core.TerrorDirector;
import com.epitech.fnah.entity.Henry;
import com.epitech.fnah.entity.Mimi;
import com.epitech.fnah.entity.Pepin;
import com.epitech.fnah.entity.ZigEtZag;
import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.Room;
import com.epitech.fnah.model.Safe;
import javafx.animation.PauseTransition;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Musiques, ambiances et effets sonores, déclenchés par les événements de la partie.
 *
 * <p>Écoute l'{@link com.epitech.fnah.core.EventBus} : la logique du jeu ne
 * connaît pas le son. Chaque bruit est placé à gauche ou à droite selon sa
 * provenance, et son volume suit la distance (Bobo est plus fort à mesure qu'il
 * approche). Les rires et voix sont joués à des hauteurs variables pour ne
 * jamais sonner deux fois pareil. La musique de nuit s'interrompt une heure
 * sur deux, et le metteur en scène de la peur impose des silences complets
 * suivis d'un coup sec.</p>
 *
 * <p>Fichiers lus dans {@code src/main/resources/audio/} : {@code menu} et
 * {@code nuit} ({@code .mp3} prioritaire s'il est lisible, sinon {@code .wav})
 * et les effets du dossier {@code sfx/}. Un fichier absent est ignoré sans erreur.
 * Le volume général vient de la progression du joueur.</p>
 *
 * <p>Au lancement, la sortie audio de la machine est vérifiée. Si elle est
 * absente (cas fréquent sous WSL), le jeu le signale et continue sans son.</p>
 */
public final class SoundManager {

    private static final String[] EFFECTS = {"door_close", "door_open", "tablet_up", "tablet_down",
        "camera_switch", "static_loop", "klaxon", "footsteps", "scream", "chime_six", "hour_bell",
        "menu_move", "menu_select", "jack_pop", "henry_entry", "heartbeat", "rain_loop", "thunder",
        "whisper", "child_laugh", "typewriter", "tape_hiss", "breaker", "light_buzz", "organ_loop",
        "crank", "henry_melody", "crash_buzz", "honk", "door_creak", "cup_fall", "sting", "wind_loop",
        "fire_loop", "thud", "zap", "dial_tick", "safe_open", "rustle",
        "girl_1", "girl_2", "girl_3", "girl_4", "laugh_1", "laugh_2", "laugh_3", "laugh_4"};
    /** Effets joués en boucle tant qu'on ne les arrête pas. */
    private static final Set<String> LOOPS = Set.of("static_loop", "rain_loop", "tape_hiss", "light_buzz",
            "organ_loop", "crank", "henry_melody", "wind_loop", "fire_loop");
    private static final double LEFT = -0.7;
    private static final double MENU_MUSIC_VOLUME = 0.6;
    private static final double NIGHT_MUSIC_VOLUME = 0.6;

    private final Game game;
    private final GameConfig config;
    private final MediaPlayer menuMusic;
    private final MediaPlayer nightMusic;
    private final Map<String, AudioClip> effects = new HashMap<>();
    private final Map<String, Double> loopVolumes = new HashMap<>();
    private final Random random = new Random();
    private final boolean available;
    private boolean muted;
    private boolean silenced;
    private boolean intro;

    /**
     * Charge les sons et s'abonne aux événements de la partie.
     *
     * @param game   partie en cours
     * @param config progression du joueur (volume général)
     */
    public SoundManager(Game game, GameConfig config) {
        this.game = game;
        this.config = config;
        this.available = audioOutputAvailable();
        if (!available) {
            System.err.println("Aucune sortie audio détectée : le jeu continue sans son.");
            System.err.println("Sous WSL : sudo apt install libasound2-plugins, puis voir la section Sound du README.");
        }
        this.menuMusic = available ? loadMusic("menu") : null;
        this.nightMusic = available ? loadMusic("nuit") : null;
        for (String name : available ? EFFECTS : new String[0]) {
            URL url = SoundManager.class.getResource("/audio/sfx/" + name + ".wav");
            if (url == null) {
                System.err.println("Effet sonore manquant : " + name + ".wav");
                continue;
            }
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
        applyMusicVolume();

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
        events.subscribe(GameEvent.FakeHornPlayed.class, e -> play("klaxon", 0.65, LEFT, 0.85 + random.nextDouble() * 0.3));
        events.subscribe(GameEvent.HourChanged.class, e -> onHour(e.hour()));
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
                play("laugh_" + (1 + random.nextInt(4)), 0.8, 0.2, 0.75);
            }
            refreshOrgan();
        });
        events.subscribe(GameEvent.AchievementUnlocked.class, e -> play("chime_six", 0.5, 0));
        events.subscribe(GameEvent.ScareEvent.class, e -> onScare(e.scare(), e.pan()));
        events.subscribe(GameEvent.PlayerHidden.class, e -> play("rustle", 0.7, 0));
        events.subscribe(GameEvent.FuseChanged.class, e -> {
            if (e.tripped() != null) {
                play("zap", 0.9, 0.5);
                refreshLights();
            } else {
                play("breaker", 0.6, 0.5);
            }
        });
        events.subscribe(GameEvent.FuseResetting.class, e -> {
            if (e.resetting()) {
                startLoop("crank", 0.45);
            } else {
                stopClip("crank");
            }
        });
        events.subscribe(GameEvent.SafeDial.class, e -> {
            if (e.result() == Safe.Result.OPENED) {
                play("safe_open", 0.9, 0.3);
            } else if (e.result() == Safe.Result.WRONG) {
                play("door_close", 0.4, 0.3, 1.6);
            } else {
                play("dial_tick", 0.6, 0.3);
            }
        });
        events.subscribe(GameEvent.ClueFound.class, e -> play("whisper", 0.35, 0, 1.3));
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

    /** La cassette se dérègle : un rire déformé, un grésillement, un coup. */
    public void tapeGlitch() {
        play("zap", 0.35, 0, 0.6);
        play(random.nextBoolean() ? "laugh_" + (1 + random.nextInt(4)) : "girl_" + (1 + random.nextInt(4)),
                0.95, Draw.random(-0.4, 0.4), 0.55 + random.nextDouble() * 0.15);
        play("sting", 0.6, 0);
    }

    /** Coup de tonnerre (orage). */
    public void thunder() {
        play("thunder", 0.9, Draw.random(-0.4, 0.4));
    }

    /** Chuchotement tout près (hallucination). */
    public void whisper() {
        play("whisper", 0.7, Draw.random(-0.8, 0.8));
    }

    /** Petite fille qui rit, tout près (hallucination). */
    public void laugh() {
        play("girl_" + (1 + random.nextInt(4)), 0.55, Draw.random(-0.8, 0.8), 0.9 + random.nextDouble() * 0.2);
    }

    /** « Pouet » du nez de clown (easter egg). */
    public void honk() {
        play("honk", 0.8, 0);
    }

    /** Coup sourd (titres, porte qui claque derrière soi). */
    public void thud() {
        play("thud", 0.9, 0);
    }

    /** Rire déformé de clown (introduction, apparitions). */
    public void clownLaugh() {
        play("laugh_" + (1 + random.nextInt(4)), 0.9, Draw.random(-0.3, 0.3), 0.7 + random.nextDouble() * 0.15);
    }

    /** Faux plantage : tout se coupe, il ne reste qu'un bourdonnement. */
    public void crash() {
        if (nightMusic != null) {
            nightMusic.pause();
        }
        stopLoops();
        play("crash_buzz", 0.5, 0);
    }

    /**
     * Ambiance de l'introduction : vent, puis feu, puis plus rien.
     *
     * @param wind {@code true} pour le vent
     * @param fire {@code true} pour l'incendie
     */
    public void introAmbience(boolean wind, boolean fire) {
        intro = wind || fire;
        if (intro) {
            stop(menuMusic);
        }
        setLoop("wind_loop", wind, 0.5);
        setLoop("fire_loop", fire, 0.6);
    }

    /** Boîte à musique de l'introduction. */
    public void musicBox() {
        play("henry_entry", 0.6, 0);
    }

    /** Fin de l'introduction : retour à la musique du menu. */
    public void endIntro() {
        intro = false;
        stopClip("wind_loop");
        stopClip("fire_loop");
        if (game.getState() == GameState.MENU) {
            play(menuMusic);
        }
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
     * Monte ou baisse le volume général et l'enregistre.
     *
     * @param step +1 pour monter, -1 pour baisser
     */
    public void adjustVolume(int step) {
        config.adjustVolume(step);
        applyMusicVolume();
        for (Map.Entry<String, Double> loop : loopVolumes.entrySet()) {
            AudioClip clip = effects.get(loop.getKey());
            if (clip != null && clip.isPlaying()) {
                clip.stop();
                clip.play(loop.getValue() * master());
            }
        }
        play("menu_move", 0.5, 0);
    }

    /**
     * Renvoie le volume général.
     *
     * @return volume entre 0 et 100
     */
    public int getVolume() {
        return config.getVolume();
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

    // ------------------------------------------------------------------ événements

    private void onState(GameState state) {
        switch (state) {
            case MENU, NIGHT_INTRO, ENDING -> {
                stop(nightMusic);
                stopLoops();
                if (!intro) {
                    play(menuMusic);
                }
            }
            case PLAYING -> {
                stop(menuMusic);
                if (!game.getPower().isPowerOut() && !silenced && game.getClock().getHour() % 2 == 0) {
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
                play("laugh_" + (1 + random.nextInt(4)), 0.7, Draw.random(-0.5, 0.5), 0.75);
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

    /** La musique joue une heure sur deux : entre-temps, seuls le vent et la pluie restent. */
    private void onHour(int hour) {
        if (hour < 6) {
            play("hour_bell", 0.45, 0);
        }
        if (game.getState() != GameState.PLAYING || game.getPower().isPowerOut() || silenced) {
            return;
        }
        if (hour % 2 == 0) {
            play(nightMusic);
        } else if (nightMusic != null) {
            nightMusic.pause();
        }
    }

    private void onScare(TerrorDirector.Scare scare, double pan) {
        double rate = 0.85 + random.nextDouble() * 0.3;
        switch (scare) {
            case FOOTSTEPS -> play("footsteps", 0.45 + random.nextDouble() * 0.35, pan, rate);
            case DOOR_CREAK -> play("door_creak", 0.5 + random.nextDouble() * 0.4, pan, rate);
            case OBJECT_FALL -> play("cup_fall", 0.85, 0.25);
            case LIGHT_FLICKER -> play("zap", 0.35, -0.2, 1.4);
            case WARPED_MUSIC -> play("organ_loop", 0.35, pan, 0.55 + random.nextDouble() * 0.15);
            case LITTLE_GIRL -> play("girl_" + (1 + random.nextInt(4)), 0.5 + random.nextDouble() * 0.3, pan, rate);
            case DISTANT_LAUGH -> play("laugh_" + (1 + random.nextInt(4)), 0.4, pan, 0.65 + random.nextDouble() * 0.2);
            case SHADOW -> play("whisper", 0.5, pan, 0.8);
            case CAMERA_FACE -> play("static_loop", 0.3, 0, 0.7);
            case POWER_SURGE -> play("zap", 0.9, 0.5);
            case SILENCE -> silence();
        }
    }

    /** Silence total pendant quelques secondes, puis un coup sec. */
    private void silence() {
        if (silenced || game.getState() != GameState.PLAYING) {
            return;
        }
        silenced = true;
        if (nightMusic != null) {
            nightMusic.pause();
        }
        stopClip("rain_loop");
        PauseTransition wait = new PauseTransition(Duration.seconds(5 + random.nextDouble() * 4));
        wait.setOnFinished(e -> {
            silenced = false;
            if (game.getState() == GameState.PLAYING) {
                play("sting", 0.8, Draw.random(-0.5, 0.5));
                resumeLoops();
                if (!game.getPower().isPowerOut() && game.getClock().getHour() % 2 == 0) {
                    play(nightMusic);
                }
            }
        });
        wait.play();
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
            play("footsteps", 0.55, -LEFT, 1.15);
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
            play("klaxon", volume, after == 1 ? -0.85 : -0.4, 0.9 + random.nextDouble() * 0.2);
        } else {
            play("footsteps", 0.6, -0.4);
        }
    }

    // ------------------------------------------------------------------ boucles

    /** Relance les boucles d'ambiance qui correspondent à l'état de la nuit. */
    private void resumeLoops() {
        if (!silenced) {
            startLoop("rain_loop", 0.22);
        }
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
        setLoop("light_buzz", office.getLeftLight().isOn() || office.getRightLight().isOn(), 0.18);
    }

    /** L'orgue de barbarie s'entend quand on regarde la caméra 8 et qu'il joue encore. */
    private void refreshOrgan() {
        Office office = game.getOffice();
        boolean audible = game.getState() == GameState.PLAYING && !game.getPower().isPowerOut()
                && office.getCameras().isWatching(Pepin.START_ROOM) && !office.getOrgan().isEmpty();
        setLoop("organ_loop", audible, 0.45);
    }

    private void setLoop(String effect, boolean on, double volume) {
        if (on) {
            startLoop(effect, volume);
        } else {
            stopClip(effect);
        }
    }

    private void stopLoops() {
        for (String loop : LOOPS) {
            stopClip(loop);
        }
    }

    private void startLoop(String effect, double volume) {
        AudioClip clip = effects.get(effect);
        loopVolumes.put(effect, volume);
        if (!muted && clip != null && !clip.isPlaying()) {
            clip.play(volume * master());
        }
    }

    private void play(String effect, double volume, double balance) {
        play(effect, volume, balance, 1.0);
    }

    private void play(String effect, double volume, double balance, double rate) {
        AudioClip clip = effects.get(effect);
        if (!muted && clip != null) {
            clip.play(volume * master(), Math.max(-1, Math.min(1, balance)), rate, 0.0, 1);
        }
    }

    private void stopClip(String effect) {
        AudioClip clip = effects.get(effect);
        if (clip != null) {
            clip.stop();
        }
    }

    private double master() {
        return config.getVolume() / 100.0;
    }

    private void applyMusicVolume() {
        if (menuMusic != null) {
            menuMusic.setVolume(MENU_MUSIC_VOLUME * master());
        }
        if (nightMusic != null) {
            nightMusic.setVolume(NIGHT_MUSIC_VOLUME * master());
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

    private static MediaPlayer loadMusic(String name) {
        for (String extension : new String[]{".mp3", ".wav"}) {
            URL url = SoundManager.class.getResource("/audio/" + name + extension);
            if (url == null) {
                continue;
            }
            try {
                MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
                player.setCycleCount(MediaPlayer.INDEFINITE);
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
