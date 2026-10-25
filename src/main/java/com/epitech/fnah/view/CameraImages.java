package com.epitech.fnah.view;

import com.epitech.fnah.model.Room;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Images des caméras, chargées une fois au lancement depuis
 * {@code src/main/resources/images/cameras/<salle>.jpg}
 * (ex. {@code couloir_gauche.jpg}). Une image absente n'empêche pas de jouer :
 * l'affichage dessine alors un écran neutre à la place.
 */
final class CameraImages {

    private static final String FOLDER = "/images/cameras/";

    private final Map<Room, Image> images = new EnumMap<>(Room.class);

    private CameraImages() {
    }

    /**
     * Charge l'image de chaque salle filmée.
     *
     * @return les images trouvées
     */
    static CameraImages load() {
        CameraImages result = new CameraImages();
        for (Room room : Room.withCameras()) {
            String path = FOLDER + room.name().toLowerCase(Locale.ROOT) + ".jpg";
            try (InputStream in = CameraImages.class.getResourceAsStream(path)) {
                if (in == null) {
                    System.err.println("Image de caméra manquante : " + path);
                    continue;
                }
                Image image = new Image(in);
                if (!image.isError()) {
                    result.images.put(room, image);
                }
            } catch (IOException e) {
                System.err.println("Image de caméra illisible : " + path + " (" + e.getMessage() + ")");
            }
        }
        return result;
    }

    /**
     * @param room salle filmée
     * @return son image, ou {@code null} si elle n'a pas pu être chargée
     */
    Image get(Room room) {
        return images.get(room);
    }
}
