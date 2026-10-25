package com.epitech.fnah.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Tablette de surveillance de la régie.
 *
 * <p>Tant qu'elle est levée, le joueur voit une caméra du chapiteau mais plus
 * ses portes, et la tablette consomme de l'énergie. Le joueur choisit la caméra
 * affichée ; la dernière caméra choisie est conservée quand la tablette est baissée.</p>
 */
public class CameraSystem implements PowerConsumer {

    /** Points d'usage ajoutés à la consommation quand la tablette est levée. */
    public static final int POWER_USAGE_WHEN_OPEN = 1;
    /** Caméra affichée en début de nuit. */
    public static final Room DEFAULT_CAMERA = Room.PISTE_CENTRALE;

    private boolean open;
    private Room selected = DEFAULT_CAMERA;
    private final Map<Room, Double> disabledCameras = new EnumMap<>(Room.class);
    private final List<Consumer<CameraSystem>> tabletListeners = new ArrayList<>();
    private final List<Consumer<CameraSystem>> selectionListeners = new ArrayList<>();

    /** Crée une tablette baissée, réglée sur la caméra {@link #DEFAULT_CAMERA}. */
    public CameraSystem() {
    }

    /** Lève la tablette si elle est baissée, la baisse sinon. */
    public void toggleTablet() {
        open = !open;
        notifyAll(tabletListeners);
    }

    /**
     * Affiche une caméra. Sans effet si elle est déjà affichée.
     *
     * @param room salle à afficher
     * @throws IllegalArgumentException si aucune caméra ne filme cette salle
     */
    public void select(Room room) {
        Objects.requireNonNull(room, "room");
        if (!room.hasCamera()) {
            throw new IllegalArgumentException(room.getDisplayName() + " has no camera");
        }
        if (room == selected) {
            return;
        }
        selected = room;
        notifyAll(selectionListeners);
    }

    /**
     * Affiche la caméra suivante ou précédente, en revenant au début après la dernière.
     *
     * @param step +1 pour la suivante, -1 pour la précédente
     */
    public void cycle(int step) {
        List<Room> cameras = Room.withCameras();
        int index = Math.floorMod(cameras.indexOf(selected) + step, cameras.size());
        select(cameras.get(index));
    }

    /**
     * Indique si le joueur regarde une salle en ce moment.
     *
     * @param room salle à tester
     * @return {@code true} si la tablette est levée sur la caméra de cette salle
     */
    public boolean isWatching(Room room) {
        return open && selected == room && !isDisabled(room);
    }

    /**
     * Brouille une caméra pendant une durée donnée (farce de Zig et Zag).
     *
     * @param room            salle dont la caméra est brouillée
     * @param durationSeconds durée du brouillage, en secondes
     * @throws IllegalArgumentException si la salle n'a pas de caméra ou si la durée n'est pas positive
     */
    public void disable(Room room, double durationSeconds) {
        Objects.requireNonNull(room, "room");
        if (!room.hasCamera()) {
            throw new IllegalArgumentException(room.getDisplayName() + " has no camera");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be > 0");
        }
        disabledCameras.put(room, durationSeconds);
    }

    /**
     * Indique si la caméra d'une salle est brouillée.
     *
     * @param room salle
     * @return {@code true} si sa caméra n'affiche que de la neige
     */
    public boolean isDisabled(Room room) {
        return disabledCameras.getOrDefault(room, 0.0) > 0;
    }

    /**
     * Renvoie le temps de brouillage restant d'une caméra.
     *
     * @param room salle
     * @return secondes restantes, 0 si la caméra fonctionne
     */
    public double getDisabledTimeRemaining(Room room) {
        return disabledCameras.getOrDefault(room, 0.0);
    }

    /**
     * Fait avancer les minuteurs de brouillage.
     *
     * @param deltaSeconds temps écoulé, en secondes
     * @throws IllegalArgumentException si le temps est négatif
     */
    public void update(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        disabledCameras.replaceAll((room, remaining) -> Math.max(0, remaining - deltaSeconds));
        disabledCameras.values().removeIf(remaining -> remaining == 0);
    }

    /**
     * Indique si la tablette est levée.
     *
     * @return {@code true} si la tablette est levée
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Renvoie la salle de la caméra choisie.
     *
     * @return la salle affichée quand la tablette est levée
     */
    public Room getSelected() {
        return selected;
    }

    /**
     * Renvoie la consommation de la tablette.
     *
     * @return {@value #POWER_USAGE_WHEN_OPEN} si elle est levée, 0 sinon
     */
    @Override
    public int getPowerUsage() {
        return open ? POWER_USAGE_WHEN_OPEN : 0;
    }

    /** Baisse la tablette et revient à la caméra par défaut, sans prévenir les écouteurs. */
    public void reset() {
        open = false;
        selected = DEFAULT_CAMERA;
        disabledCameras.clear();
    }

    /**
     * Enregistre un écouteur appelé quand la tablette est levée ou baissée.
     *
     * @param listener reçoit la tablette
     */
    public void onTabletToggle(Consumer<CameraSystem> listener) {
        tabletListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Enregistre un écouteur appelé quand une autre caméra est choisie.
     *
     * @param listener reçoit la tablette
     */
    public void onCameraSelected(Consumer<CameraSystem> listener) {
        selectionListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void notifyAll(List<Consumer<CameraSystem>> listeners) {
        for (Consumer<CameraSystem> listener : listeners) {
            listener.accept(this);
        }
    }
}
