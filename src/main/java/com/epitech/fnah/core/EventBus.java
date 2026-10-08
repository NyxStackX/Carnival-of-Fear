package com.epitech.fnah.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Canal d'événements entre la logique et le reste du programme (patron Observer).
 *
 * <p>La logique publie des {@link GameEvent} sans savoir qui les écoute.
 * L'affichage, et plus tard le son, s'abonnent aux types qui les intéressent.
 * Ajouter un écouteur ne demande donc jamais de modifier la logique.</p>
 */
public class EventBus {

    private final Map<Class<? extends GameEvent>, List<Consumer<GameEvent>>> listeners = new HashMap<>();

    /** Crée un canal sans aucun abonné. */
    public EventBus() {
    }

    /**
     * Abonne un écouteur à un type d'événement.
     *
     * @param type     type d'événement écouté (ex. {@code GameEvent.StateChanged.class})
     * @param listener appelé à chaque publication de ce type
     * @param <E>      type d'événement
     */
    public <E extends GameEvent> void subscribe(Class<E> type, Consumer<? super E> listener) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(listener, "listener");
        listeners.computeIfAbsent(type, k -> new ArrayList<>())
                .add(event -> listener.accept(type.cast(event)));
    }

    /**
     * Transmet un événement à tous les écouteurs de son type, dans l'ordre d'abonnement.
     *
     * @param event événement à publier
     */
    public void publish(GameEvent event) {
        Objects.requireNonNull(event, "event");
        List<Consumer<GameEvent>> targets = listeners.get(event.getClass());
        if (targets == null) {
            return;
        }
        for (Consumer<GameEvent> listener : List.copyOf(targets)) {
            listener.accept(event);
        }
    }
}
