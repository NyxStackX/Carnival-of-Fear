package com.epitech.fnah.core;

import com.epitech.fnah.config.CustomNight;
import com.epitech.fnah.config.NightConfig;
import com.epitech.fnah.entity.Bobo;
import com.epitech.fnah.entity.Dice;
import com.epitech.fnah.entity.Henry;
import com.epitech.fnah.entity.Mimi;
import com.epitech.fnah.entity.Pepin;
import com.epitech.fnah.entity.RandomDice;
import com.epitech.fnah.entity.ZigEtZag;
import com.epitech.fnah.model.CameraSystem;
import com.epitech.fnah.model.FuseBox;
import com.epitech.fnah.model.Light;
import com.epitech.fnah.model.MapGraph;
import com.epitech.fnah.model.Office;
import com.epitech.fnah.model.PowerSystem;
import com.epitech.fnah.model.Room;
import com.epitech.fnah.model.Safe;

import java.util.Objects;

/**
 * Point d'entrée de la logique du jeu pour l'affichage (patron Facade).
 *
 * <p>Crée et relie l'horloge, l'énergie, la régie, le plan et les clowns,
 * gère l'enchaînement des nuits, et publie ce qui se passe sur l'{@link EventBus}.
 * L'affichage n'a besoin que de cette classe.</p>
 */
public final class Game {

    /** Bruit d'une porte qui claque. */
    public static final double DOOR_NOISE = 15.0;
    /** Bruit d'un mauvais code sur le coffre. */
    public static final double WRONG_CODE_NOISE = 10.0;
    /** Énergie rendue par la batterie du coffre, en pourcentage. */
    public static final double SAFE_ENERGY = 20.0;

    private final GameClock clock = new GameClock();
    private final PowerSystem power = new PowerSystem();
    private final Office office = new Office();
    private final MapGraph map = MapGraph.circus();
    private final EventBus events = new EventBus();
    private final Bobo bobo;
    private final Mimi mimi;
    private final ZigEtZag zigEtZag;
    private final Pepin pepin;
    private final Henry henry;
    private final TerrorDirector director;
    private final Dice dice;
    private final GameLoop loop;

    private int currentNight = 1;
    private CustomNight customNight;

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
        this.mimi = new Mimi(1, dice);
        this.zigEtZag = new ZigEtZag(1, dice);
        this.pepin = new Pepin(0, dice);
        this.henry = new Henry(dice);
        this.director = new TerrorDirector(dice);
        this.dice = dice;
        this.loop = new GameLoop(clock, power, office, map, events::publish);
        loop.registerSystem(bobo);
        loop.registerSystem(mimi);
        loop.registerSystem(zigEtZag);
        loop.registerSystem(pepin);
        loop.registerSystem(director);
        loop.registerBlackoutSystem(henry);
        loop.registerBlackoutSystem(director);

        loop.onStateChange(state -> events.publish(new GameEvent.StateChanged(state)));
        bobo.onMove((from, to) -> events.publish(new GameEvent.AnimatronicMoved(bobo.getName(), from, to)));
        mimi.onMove((from, to) -> events.publish(new GameEvent.AnimatronicMoved(mimi.getName(), from, to)));
        zigEtZag.onMove((from, to) -> events.publish(new GameEvent.AnimatronicMoved(zigEtZag.getName(), from, to)));
        office.getLeftDoor().onToggle(door -> events.publish(new GameEvent.DoorToggled(door.getSide(), door.isClosed())));
        office.getRightDoor().onToggle(door -> events.publish(new GameEvent.DoorToggled(door.getSide(), door.isClosed())));
        office.getLeftLight().onToggle(light -> events.publish(new GameEvent.LightToggled(light.getSide(), light.isOn())));
        office.getRightLight().onToggle(light -> events.publish(new GameEvent.LightToggled(light.getSide(), light.isOn())));
        clock.onHourChange(hour -> events.publish(new GameEvent.HourChanged(hour)));
        office.getCameras().onTabletToggle(cams -> events.publish(new GameEvent.TabletToggled(cams.isOpen(), cams.getSelected())));
        office.getCameras().onCameraSelected(cams -> events.publish(new GameEvent.CameraSelected(cams.getSelected())));
        office.getLeftDoor().onToggle(door -> office.getNoise().make(DOOR_NOISE));
        office.getRightDoor().onToggle(door -> office.getNoise().make(DOOR_NOISE));
        office.getCameras().onTabletToggle(cams -> lookForClue());
        office.getCameras().onCameraSelected(cams -> lookForClue());
        office.getFuseBox().onChange(this::onFuseChanged);
    }

    /** Un chiffre du code est-il écrit sur la caméra regardée ? */
    private void lookForClue() {
        CameraSystem cameras = office.getCameras();
        int index = Safe.clueIndex(cameras.getSelected());
        if (index >= 0 && cameras.isWatching(cameras.getSelected()) && office.getSafe().discover(index)) {
            events.publish(new GameEvent.ClueFound(index, office.getSafe().digit(index)));
        }
    }

    /** Un circuit saute : ce qu'il alimente s'éteint aussitôt. */
    private void onFuseChanged(FuseBox fuses) {
        if (fuses.isTripped(FuseBox.Circuit.CAMERAS) && office.getCameras().isOpen()) {
            setWinding(false);
            office.getCameras().toggleTablet();
        }
        if (fuses.isTripped(FuseBox.Circuit.LIGHTS)) {
            office.getLeftLight().setOn(false);
            office.getRightLight().setOn(false);
        }
        events.publish(new GameEvent.FuseChanged(fuses.getTripped()));
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
        customNight = null;
        bobo.setBaseAiLevel(config.boboAiLevel());
        mimi.setBaseAiLevel(config.mimiAiLevel());
        zigEtZag.setBaseAiLevel(config.zigEtZagAiLevel());
        pepin.setBaseAiLevel(config.pepinAiLevel());
        power.setEnergyMultiplier(config.energyMultiplier());
        director.setNight(night);
        loop.startNight();
        newSafeCode();
        events.publish(new GameEvent.NightStarted(night, false, false));
    }

    /**
     * Lance la nuit personnalisée, avec le niveau choisi pour chaque clown.
     * Elle compte comme une nuit après la dernière : il n'y a pas de nuit suivante.
     *
     * @param custom niveaux des clowns
     */
    public void startCustomNight(CustomNight custom) {
        customNight = Objects.requireNonNull(custom, "custom");
        currentNight = NightConfig.LAST_NIGHT;
        bobo.setBaseAiLevel(custom.bobo());
        mimi.setBaseAiLevel(custom.mimi());
        zigEtZag.setBaseAiLevel(custom.zigEtZag());
        pepin.setBaseAiLevel(custom.pepin());
        power.setEnergyMultiplier(CustomNight.ENERGY_MULTIPLIER);
        director.setNight(NightConfig.LAST_NIGHT);
        loop.startNight();
        newSafeCode();
        events.publish(new GameEvent.NightStarted(currentNight, true, custom.isMaxChallenge()));
    }

    private void newSafeCode() {
        office.getSafe().setCode(dice.rollD20() % 10, dice.rollD20() * 7 % 10, dice.rollD20() * 3 % 10);
    }

    /** Relance la nuit en cours (ou la nuit personnalisée) depuis le début. */
    public void restartNight() {
        if (customNight != null) {
            startCustomNight(customNight);
        } else {
            startNight(currentNight);
        }
    }

    /**
     * Indique si la nuit en cours est la nuit personnalisée.
     *
     * @return {@code true} pour la nuit personnalisée
     */
    public boolean isCustomNight() {
        return customNight != null;
    }

    /**
     * Indique si le veilleur a des hallucinations cette nuit (nuit 3 et suivantes,
     * et nuit personnalisée).
     *
     * @return {@code true} si les hallucinations sont actives
     */
    public boolean hasHallucinations() {
        return customNight != null || NightConfig.forNight(currentNight).hasHallucinations();
    }

    /**
     * Renvoie {@code true} s'il reste une nuit après la nuit en cours.
     *
     * @return {@code true} s'il reste une nuit après la nuit en cours
     */
    public boolean hasNextNight() {
        return customNight == null && currentNight < NightConfig.LAST_NIGHT;
    }

    /**
     * Indique si la fin de l'histoire peut être jouée : la nuit 5 vient d'être gagnée.
     *
     * @return {@code true} après la victoire de la nuit 5
     */
    public boolean canPlayEnding() {
        return loop.getState() == GameState.WIN && customNight == null && currentNight == NightConfig.LAST_NIGHT;
    }

    /**
     * Passe à la fin de l'histoire (journal puis générique).
     *
     * @throws IllegalStateException si la nuit 5 n'a pas été gagnée juste avant
     */
    public void playEnding() {
        if (!canPlayEnding()) {
            throw new IllegalStateException("ending only after winning night " + NightConfig.LAST_NIGHT);
        }
        loop.setState(GameState.ENDING);
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

    /** Met la nuit en pause. */
    public void pause() {
        loop.pause();
    }

    /** Reprend la nuit après une pause. */
    public void resume() {
        loop.resume();
    }

    /**
     * Ouvre ou ferme la porte gauche. Sans effet si aucune nuit n'est en cours,
     * ou si la tablette est levée : le joueur doit la baisser pour atteindre la porte.
     */
    public void toggleLeftDoor() {
        if (canReachDesk()) {
            office.getLeftDoor().toggle();
        }
    }

    /** Ouvre ou ferme la porte droite. Mêmes conditions que la porte gauche. */
    public void toggleRightDoor() {
        if (canReachDesk()) {
            office.getRightDoor().toggle();
        }
    }

    /**
     * Allume ou éteint la lampe du couloir gauche. Pour allumer, il faut une nuit
     * en cours, la tablette baissée et du courant. Éteindre est toujours possible.
     *
     * @param on {@code true} pour allumer (touche maintenue)
     */
    public void setLeftLight(boolean on) {
        setLight(office.getLeftLight(), on);
    }

    /**
     * Allume ou éteint la lampe du couloir droit. Mêmes conditions que {@link #setLeftLight(boolean)}.
     *
     * @param on {@code true} pour allumer (touche maintenue)
     */
    public void setRightLight(boolean on) {
        setLight(office.getRightLight(), on);
    }

    private void setLight(Light light, boolean on) {
        light.setOn(on && canReachDesk() && !office.getFuseBox().isTripped(FuseBox.Circuit.LIGHTS));
    }

    /**
     * Prend ou lâche la manivelle de l'orgue. Il faut regarder la caméra 8 pour la prendre.
     *
     * @param winding {@code true} pour tourner la manivelle (touche maintenue)
     */
    public void setWinding(boolean winding) {
        boolean can = winding && canUseCameras() && office.getCameras().getSelected() == Pepin.START_ROOM;
        if (office.getOrgan().isWinding() != can) {
            office.getOrgan().setWinding(can);
            events.publish(new GameEvent.OrganWinding(can));
        }
    }

    /** Relâche toutes les commandes maintenues (lampes, manivelle), par exemple si la fenêtre perd le focus. */
    public void releaseControls() {
        setLeftLight(false);
        setRightLight(false);
        setWinding(false);
        setFuseReset(false);
    }

    /**
     * Lève ou baisse la tablette des caméras. Sans effet si aucune nuit n'est en cours,
     * sans courant, si le joueur est caché ou si le fusible des caméras a sauté
     * (la baisser reste possible).
     */
    public void toggleTablet() {
        boolean open = office.getCameras().isOpen();
        boolean blocked = !open && (office.isHidden() || office.getFuseBox().isTripped(FuseBox.Circuit.CAMERAS));
        if (loop.getState() == GameState.PLAYING && !power.isPowerOut() && !blocked) {
            releaseControls();
            office.getCameras().toggleTablet();
        }
    }

    /**
     * Se cache sous le bureau ou en sort. Caché, le joueur ne peut plus rien
     * utiliser, mais Pépin ne le trouve pas s'il reste silencieux. Impossible
     * pendant une coupure de courant.
     */
    public void toggleHide() {
        if (loop.getState() != GameState.PLAYING || power.isPowerOut()) {
            return;
        }
        boolean hide = !office.isHidden();
        if (hide) {
            releaseControls();
            if (office.getCameras().isOpen()) {
                office.getCameras().toggleTablet();
            }
        }
        office.setHidden(hide);
        events.publish(new GameEvent.PlayerHidden(hide));
    }

    /**
     * Prend ou lâche le levier de la boîte à fusibles (touche maintenue). Il faut
     * être au bureau et qu'un circuit ait sauté. Réarmer fait du bruit.
     *
     * @param holding {@code true} pour tenir le levier
     */
    public void setFuseReset(boolean holding) {
        FuseBox fuses = office.getFuseBox();
        boolean can = holding && canReachDesk() && fuses.getTripped() != null;
        if (fuses.isResetting() != can) {
            fuses.setResetting(can);
            events.publish(new GameEvent.FuseResetting(can));
        }
    }

    /**
     * Tape un chiffre sur le cadran du coffre (au bureau). Un code faux fait du
     * bruit ; le bon code ouvre le coffre et donne la batterie de secours.
     *
     * @param digit chiffre entre 0 et 9
     * @return résultat, ou {@code null} si le coffre est hors de portée
     */
    public Safe.Result dialSafe(int digit) {
        if (!canReachDesk()) {
            return null;
        }
        Safe.Result result = office.getSafe().press(digit);
        if (result == Safe.Result.OPENED) {
            power.recharge(SAFE_ENERGY);
        } else if (result == Safe.Result.WRONG) {
            office.getNoise().make(WRONG_CODE_NOISE);
        }
        events.publish(new GameEvent.SafeDial(result));
        return result;
    }

    /**
     * Indique si un danger est tout proche : un clown juste derrière une porte,
     * Pépin hors de sa boîte ou Henry dans le noir. Sert au battement de cœur.
     *
     * @return {@code true} si un danger est tout proche
     */
    public boolean isThreatNear() {
        return bobo.isAtDoor() || mimi.getRoom() == Room.COULOIR_DROIT
                || pepin.isOut() || henry.isPlayingMelody();
    }

    private boolean canReachDesk() {
        return loop.getState() == GameState.PLAYING && !office.getCameras().isOpen() && !power.isPowerOut()
                && !office.isHidden();
    }

    /**
     * Affiche une caméra. Sans effet si aucune nuit n'est en cours, si la tablette
     * est baissée, ou si aucune caméra ne porte ce numéro.
     *
     * @param number numéro de caméra (1 à {@value Room#CAMERA_COUNT})
     */
    public void selectCamera(int number) {
        if (canUseCameras()) {
            Room.fromCameraNumber(number).ifPresent(office.getCameras()::select);
            releaseCrankIfAway();
        }
    }

    /**
     * Passe à la caméra suivante ou précédente. Mêmes conditions que {@link #selectCamera(int)}.
     *
     * @param step +1 pour la suivante, -1 pour la précédente
     */
    public void cycleCamera(int step) {
        if (canUseCameras()) {
            office.getCameras().cycle(step);
            releaseCrankIfAway();
        }
    }

    private void releaseCrankIfAway() {
        if (office.getCameras().getSelected() != Pepin.START_ROOM) {
            setWinding(false);
        }
    }

    private boolean canUseCameras() {
        return loop.getState() == GameState.PLAYING && office.getCameras().isOpen();
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
     * Renvoie Bobo.
     *
     * @return Bobo
     */
    public Bobo getBobo() { return bobo; }

    /**
     * Renvoie Mimi.
     *
     * @return Mimi
     */
    public Mimi getMimi() { return mimi; }

    /**
     * Renvoie Zig et Zag.
     *
     * @return Zig et Zag
     */
    public ZigEtZag getZigEtZag() { return zigEtZag; }

    /**
     * Renvoie Pépin.
     *
     * @return Pépin
     */
    public Pepin getPepin() { return pepin; }

    /**
     * Renvoie Henry.
     *
     * @return Henry
     */
    public Henry getHenry() { return henry; }

    /**
     * Renvoie le metteur en scène de la peur.
     *
     * @return le metteur en scène
     */
    public TerrorDirector getDirector() { return director; }

}
