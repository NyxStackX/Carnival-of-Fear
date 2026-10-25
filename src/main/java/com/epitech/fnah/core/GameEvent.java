package com.epitech.fnah.core;

import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.Room;

/**
 * Événements publiés sur l'{@link EventBus}. Ce sont des records immuables :
 * ils décrivent ce qui s'est passé, sans référence vers les objets du jeu.
 */
public sealed interface GameEvent {

    /**
     * L'état du jeu a changé.
     *
     * @param state nouvel état
     */
    record StateChanged(GameState state) implements GameEvent {
    }

    /**
     * Un clown a changé de salle.
     *
     * @param name nom du clown
     * @param from salle quittée
     * @param to   salle atteinte
     */
    record AnimatronicMoved(String name, Room from, Room to) implements GameEvent {
    }

    /**
     * Une porte a été ouverte ou fermée.
     *
     * @param side   côté de la porte
     * @param closed {@code true} si elle vient d'être fermée
     */
    record DoorToggled(Door.Side side, boolean closed) implements GameEvent {
    }

    /**
     * Une nouvelle heure de jeu commence.
     *
     * @param hour heure atteinte (1 à 6)
     */
    record HourChanged(int hour) implements GameEvent {
    }

    /**
     * La tablette des caméras a été levée ou baissée.
     *
     * @param open   {@code true} si elle vient d'être levée
     * @param camera salle affichée par la tablette
     */
    record TabletToggled(boolean open, Room camera) implements GameEvent {
    }

    /**
     * Le joueur a choisi une autre caméra.
     *
     * @param camera salle désormais affichée
     */
    record CameraSelected(Room camera) implements GameEvent {
    }
}
