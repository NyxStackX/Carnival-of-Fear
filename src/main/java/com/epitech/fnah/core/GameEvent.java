package com.epitech.fnah.core;

import com.epitech.fnah.config.Achievement;
import com.epitech.fnah.model.Door;
import com.epitech.fnah.model.FuseBox;
import com.epitech.fnah.model.Room;
import com.epitech.fnah.model.Safe;

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

    /** Zig et Zag ont joué un faux klaxon. */
    record FakeHornPlayed() implements GameEvent {
    }

    /**
     * Une nuit commence.
     *
     * @param night        numéro de la nuit
     * @param custom       {@code true} pour la nuit personnalisée
     * @param maxChallenge {@code true} si tous les clowns sont au niveau 20
     */
    record NightStarted(int night, boolean custom, boolean maxChallenge) implements GameEvent {
    }

    /**
     * Une lampe de couloir s'est allumée ou éteinte.
     *
     * @param side côté de la lampe
     * @param on   {@code true} si elle vient de s'allumer
     */
    record LightToggled(Door.Side side, boolean on) implements GameEvent {
    }

    /** L'énergie est tombée à 0 % : tout s'éteint. */
    record PowerOut() implements GameEvent {
    }

    /**
     * Henry commence ou arrête sa mélodie dans le noir.
     *
     * @param playing {@code true} quand la mélodie commence
     */
    record HenryMelody(boolean playing) implements GameEvent {
    }

    /**
     * Le joueur prend ou lâche la manivelle de l'orgue.
     *
     * @param winding {@code true} quand il commence à tourner
     */
    record OrganWinding(boolean winding) implements GameEvent {
    }

    /**
     * Pépin sort de sa boîte, ou y retourne.
     *
     * @param out {@code true} quand il sort
     */
    record PepinEscaped(boolean out) implements GameEvent {
    }

    /**
     * Le joueur vient d'obtenir une étoile.
     *
     * @param achievement étoile obtenue
     */
    record AchievementUnlocked(Achievement achievement) implements GameEvent {
    }

    /**
     * Le metteur en scène de la peur déclenche un événement.
     *
     * @param scare ce qui arrive
     * @param pan   direction du son, de -1 (gauche) à 1 (droite)
     */
    record ScareEvent(TerrorDirector.Scare scare, double pan) implements GameEvent {
    }

    /**
     * Le joueur se cache sous le bureau ou en sort.
     *
     * @param hidden {@code true} quand il se cache
     */
    record PlayerHidden(boolean hidden) implements GameEvent {
    }

    /**
     * Un fusible saute ou est réarmé.
     *
     * @param tripped circuit coupé, ou {@code null} quand tout remarche
     */
    record FuseChanged(FuseBox.Circuit tripped) implements GameEvent {
    }

    /**
     * Le joueur prend ou lâche le levier du disjoncteur.
     *
     * @param resetting {@code true} quand il commence
     */
    record FuseResetting(boolean resetting) implements GameEvent {
    }

    /**
     * Un chiffre du code du coffre vient d'être repéré sur une caméra.
     *
     * @param index rang du chiffre (0 à 2)
     * @param digit le chiffre
     */
    record ClueFound(int index, int digit) implements GameEvent {
    }

    /**
     * Un chiffre a été tapé sur le cadran du coffre.
     *
     * @param result ce qu'il s'est passé
     */
    record SafeDial(Safe.Result result) implements GameEvent {
    }
}
