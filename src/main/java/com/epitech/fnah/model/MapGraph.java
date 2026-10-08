package com.epitech.fnah.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Plan du chapiteau sous forme de graphe non orienté : chaque salle est un nœud,
 * chaque passage une arête.
 *
 * <p>Les clowns calculent leur chemin par un parcours en largeur (BFS), qui donne
 * le plus court chemin en nombre de salles. La régie n'est jamais traversée :
 * on peut y arriver, mais pas passer par elle pour aller ailleurs.</p>
 */
public class MapGraph {

    private final Map<Room, List<Room>> adjacency = new EnumMap<>(Room.class);

    /** Crée un plan vide : toutes les salles existent, sans aucun passage. */
    public MapGraph() {
        for (Room room : Room.values()) {
            adjacency.put(room, new ArrayList<>());
        }
    }

    /**
     * Crée le plan du Cirque Malvaux décrit dans le GDD.
     *
     * @return le plan complet des 8 salles et de la régie
     */
    public static MapGraph circus() {
        MapGraph map = new MapGraph();
        map.connect(Room.PISTE_CENTRALE, Room.GRADINS);
        map.connect(Room.PISTE_CENTRALE, Room.LOGES);
        map.connect(Room.GRADINS, Room.COULISSES_OUEST);
        map.connect(Room.COULISSES_OUEST, Room.COULOIR_GAUCHE);
        map.connect(Room.COULOIR_GAUCHE, Room.REGIE);
        map.connect(Room.LOGES, Room.MENAGERIE);
        map.connect(Room.MENAGERIE, Room.COULOIR_DROIT);
        map.connect(Room.COULOIR_DROIT, Room.REGIE);
        map.connect(Room.FOSSE_ORCHESTRE, Room.REGIE);
        return map;
    }

    /**
     * Ajoute un passage dans les deux sens. Sans effet s'il existe déjà.
     *
     * @param a première salle
     * @param b seconde salle
     * @throws IllegalArgumentException si {@code a} et {@code b} sont la même salle
     */
    public void connect(Room a, Room b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a == b) {
            throw new IllegalArgumentException("a room cannot be connected to itself");
        }
        if (areConnected(a, b)) {
            return;
        }
        adjacency.get(a).add(b);
        adjacency.get(b).add(a);
    }

    /**
     * Liste les salles directement reliées à une salle.
     *
     * @param room salle de départ
     * @return salles voisines, dans l'ordre où les passages ont été ajoutés
     */
    public List<Room> neighbors(Room room) {
        return Collections.unmodifiableList(adjacency.get(Objects.requireNonNull(room, "room")));
    }

    /**
     * Indique si deux salles sont directement reliées.
     *
     * @param a première salle
     * @param b seconde salle
     * @return {@code true} si un passage relie directement les deux salles
     */
    public boolean areConnected(Room a, Room b) {
        return adjacency.get(a).contains(b);
    }

    /**
     * Plus court chemin entre deux salles (BFS).
     *
     * @param from salle de départ
     * @param to   salle d'arrivée
     * @return les salles du chemin, départ et arrivée compris ; {@code [from]} si
     *         {@code from == to} ; liste vide si l'arrivée est inaccessible
     */
    public List<Room> shortestPath(Room from, Room to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Map<Room, Room> previous = new EnumMap<>(Room.class);
        Deque<Room> queue = new ArrayDeque<>();
        queue.add(from);
        previous.put(from, from);

        while (!queue.isEmpty()) {
            Room current = queue.poll();
            if (current == to) {
                return rebuildPath(previous, from, to);
            }
            if (current == Room.REGIE && current != from) {
                continue;
            }
            for (Room next : adjacency.get(current)) {
                if (!previous.containsKey(next)) {
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }
        return List.of();
    }

    /**
     * Prochaine salle sur le plus court chemin.
     *
     * @param from salle actuelle
     * @param to   destination
     * @return la salle suivante, ou vide si déjà arrivé ou si la destination est inaccessible
     */
    public Optional<Room> nextStepTowards(Room from, Room to) {
        List<Room> path = shortestPath(from, to);
        return path.size() >= 2 ? Optional.of(path.get(1)) : Optional.empty();
    }

    /**
     * Nombre de salles à traverser pour aller d'une salle à une autre.
     *
     * @param from salle de départ
     * @param to   salle d'arrivée
     * @return nombre de passages à franchir, ou -1 si l'arrivée est inaccessible
     */
    public int distance(Room from, Room to) {
        return shortestPath(from, to).size() - 1;
    }

    private static List<Room> rebuildPath(Map<Room, Room> previous, Room from, Room to) {
        List<Room> path = new ArrayList<>();
        Room step = to;
        path.add(step);
        while (step != from) {
            step = previous.get(step);
            path.add(step);
        }
        Collections.reverse(path);
        return List.copyOf(path);
    }
}
