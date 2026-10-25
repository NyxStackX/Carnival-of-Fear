package com.epitech.fnah.view;

import com.epitech.fnah.model.Room;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ressources graphiques du jeu, chargées une seule fois au lancement :
 * polices, images des caméras et sprites des clowns.
 *
 * <p>Une ressource absente n'empêche jamais de jouer : les polices retombent
 * sur des polices système et une image manquante est simplement ignorée.</p>
 */
public final class Assets {

    private final Map<Room, Image> cameras = new EnumMap<>(Room.class);
    private final Map<String, Image> sprites = new HashMap<>();
    private final Map<String, Image> scaledSprites = new HashMap<>();
    private String titleFamily = "Serif";
    private String uiFamily = "Monospaced";

    private Assets() {
    }

    /**
     * Charge toutes les ressources depuis {@code src/main/resources}.
     *
     * @return les ressources trouvées
     */
    public static Assets load() {
        Assets assets = new Assets();
        assets.titleFamily = loadFont("/fonts/Rye-Regular.ttf", assets.titleFamily);
        assets.uiFamily = loadFont("/fonts/SpecialElite-Regular.ttf", assets.uiFamily);
        for (Room room : Room.withCameras()) {
            Image image = loadImage("/images/cameras/" + room.name().toLowerCase(Locale.ROOT) + ".jpg");
            if (image != null) {
                assets.cameras.put(room, image);
            }
        }
        Map<String, String[]> frames = Map.of(
                "bobo", new String[]{"idle_1", "idle_2", "idle_3", "door", "scare_1", "scare_2"},
                "pepin", new String[]{"idle_1", "idle_2", "idle_3", "grin", "scare_1", "scare_2"});
        for (Map.Entry<String, String[]> entry : frames.entrySet()) {
            String character = entry.getKey();
            for (String frame : entry.getValue()) {
                Image image = loadImage("/sprites/" + character + "/" + frame + ".png");
                if (image != null) {
                    assets.sprites.put(character + "/" + frame, image);
                }
            }
        }
        return assets;
    }

    /**
     * Police des titres (style affiche de cirque).
     *
     * @param size taille en points
     * @return la police
     */
    public Font title(double size) {
        return Font.font(titleFamily, size);
    }

    /**
     * Police de l'interface (style machine à écrire).
     *
     * @param size taille en points
     * @return la police
     */
    public Font ui(double size) {
        return Font.font(uiFamily, size);
    }

    /**
     * Image d'une caméra.
     *
     * @param room salle filmée
     * @return l'image, ou {@code null} si elle n'a pas pu être chargée
     */
    public Image camera(Room room) {
        return cameras.get(room);
    }

    /**
     * Image d'un clown.
     *
     * @param character nom du dossier du clown (ex. {@code "bobo"})
     * @param frame     nom de l'image (ex. {@code "idle_1"})
     * @return l'image, ou {@code null} si elle n'existe pas
     */
    public Image sprite(String character, String frame) {
        return sprites.get(character + "/" + frame);
    }

    /**
     * Image d'un clown déjà redimensionnée (calculée une fois, puis gardée en mémoire).
     * Bien plus rapide à afficher qu'une image redimensionnée à chaque image.
     *
     * @param character nom du dossier du clown
     * @param frame     nom de l'image
     * @param height    hauteur voulue en pixels
     * @return l'image, ou {@code null} si elle n'existe pas
     */
    public Image sprite(String character, String frame, double height) {
        if (sprite(character, frame) == null) {
            return null;
        }
        int h = (int) Math.round(height);
        return scaledSprites.computeIfAbsent(character + "/" + frame + "@" + h, key -> {
            try (InputStream in = Assets.class.getResourceAsStream("/sprites/" + character + "/" + frame + ".png")) {
                return in == null ? null : new Image(in, 0, h, true, true);
            } catch (IOException e) {
                return null;
            }
        });
    }

    private static String loadFont(String path, String fallback) {
        try (InputStream in = Assets.class.getResourceAsStream(path)) {
            if (in == null) {
                System.err.println("Police manquante : " + path);
                return fallback;
            }
            Font font = Font.loadFont(in, 12);
            return font != null ? font.getFamily() : fallback;
        } catch (IOException e) {
            System.err.println("Police illisible : " + path);
            return fallback;
        }
    }

    private static Image loadImage(String path) {
        try (InputStream in = Assets.class.getResourceAsStream(path)) {
            if (in == null) {
                System.err.println("Image manquante : " + path);
                return null;
            }
            Image image = new Image(in);
            return image.isError() ? null : image;
        } catch (IOException e) {
            System.err.println("Image illisible : " + path);
            return null;
        }
    }
}
