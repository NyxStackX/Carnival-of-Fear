package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.model.Room;

import java.util.List;

/**
 * Zig et Zag, les jumeaux farceurs. Ils errent sous le chapiteau sans jamais
 * attaquer, mais brouillent une caméra ou jouent un faux klaxon pour tromper
 * le joueur. Regardés trop longtemps, ils retournent aux gradins.
 */
public final class ZigEtZag extends Animatronic {

    /** Nom des jumeaux. */
    public static final String NAME = "Zig et Zag";
    /** Dossier de leurs images. */
    public static final String SPRITE_ID = "zig_et_zag";
    /** Secondes entre deux tentatives d'action. */
    public static final double ACTION_INTERVAL = 6.0;
    /** Secondes de caméra avant qu'ils ne s'enfuient. */
    public static final double WATCH_TO_FLEE_DURATION = 2.0;
    /** Durée du brouillage d'une caméra, en secondes. */
    public static final double CAMERA_DISABLE_DURATION = 15.0;
    /** Salle de départ. */
    public static final Room START_ROOM = Room.GRADINS;

    private double watchedTimer;

    /**
     * Crée les jumeaux dans les gradins.
     *
     * @param baseAiLevel niveau d'IA de départ
     * @param dice        source de hasard
     */
    public ZigEtZag(int baseAiLevel, Dice dice) {
        super(NAME, START_ROOM, baseAiLevel, ACTION_INTERVAL, dice);
    }

    @Override
    protected boolean updateSpecialBehavior(double deltaSeconds, GameContext ctx) {
        if (ctx.office.getCameras().isWatching(getRoom())) {
            watchedTimer += deltaSeconds;
            if (watchedTimer >= WATCH_TO_FLEE_DURATION) {
                moveTo(START_ROOM);
                watchedTimer = 0;
                return true;
            }
            return true;
        }
        watchedTimer = 0;
        return false;
    }

    @Override
    protected void act(GameContext ctx) {
        List<Room> destinations = ctx.map.neighbors(getRoom()).stream()
                .filter(room -> room != Room.REGIE)
                .toList();
        if (!destinations.isEmpty()) {
            moveTo(destinations.get(Math.floorMod(rollD20(), destinations.size())));
        }

        if (rollD20() % 2 == 0) {
            List<Room> cameras = Room.withCameras();
            Room disrupted = cameras.get(Math.floorMod(rollD20(), cameras.size()));
            ctx.office.getCameras().disable(disrupted, CAMERA_DISABLE_DURATION);
        } else {
            ctx.publish(new GameEvent.FakeHornPlayed());
        }
    }

    @Override
    protected void resetSpecificBehavior() {
        watchedTimer = 0;
    }

}
