# Minuit au Cirque Malvaux

Survival horror en Java (projet Epitech 2D GAME). Le joueur est veilleur de nuit dans un cirque abandonné et doit survivre de 00:00 à 06:00 pendant que d'anciens clowns rôdent sous le chapiteau.

## État actuel

Version jouable avec un ennemi et deux mécaniques.

- **Bobo** avance vers la porte gauche en suivant le plus court chemin dans le plan du chapiteau.
- **La porte gauche** le repousse quand elle est fermée, mais consomme de l'énergie.
- **La tablette des caméras** montre les 8 salles du chapiteau. Elle consomme de l'énergie et, tant qu'elle est levée, la porte est hors de portée.
- Une nuit dure 6 minutes réelles. Les nuits 1 à 5 rendent Bobo de plus en plus actif.
- La progression (nuits débloquées) est sauvegardée entre deux parties.
- Une musique pour le menu, une ambiance pour la nuit, coupées au jumpscare (fichiers à déposer dans `src/main/resources/audio/`, voir le `LISEZMOI.md` du dossier).

Prochaines étapes : lumière du couloir gauche, sprites de la régie et de Bobo, puis Mimi (voir le GDD).

## Lancer le jeu

Prérequis : JDK 21 et Maven.

```bash
mvn javafx:run
```

La progression est enregistrée dans `~/.cirque-malvaux/config.properties`. Supprimer ce fichier remet le jeu à la nuit 1.

## Contrôles

| Action | Touche |
|---|---|
| Ouvrir / fermer la porte gauche | `Q` ou clic sur le bouton PORTE (tablette baissée) |
| Lever / baisser la tablette | `Espace` ou clic sur la barre CAMÉRAS |
| Choisir une caméra | `1` à `8`, `Gauche` / `Droite`, ou clic sur le plan |
| Choisir la nuit (menu) | `Gauche` / `Droite` dans le menu |
| Commencer, rejouer, nuit suivante | `Entrée` |
| Retour au menu | `Échap` |
| Couper / remettre le son | `M` |
| Accélérer le temps x10 (test) | `F2` |

## Qualité

| Commande | Résultat |
|---|---|
| `mvn test` | Lance les tests et génère le rapport de couverture dans `target/site/jacoco/index.html` |
| `mvn verify` | Comme `mvn test`, et échoue si la couverture du code logique passe sous 80 % |
| `mvn javadoc:javadoc` | Génère la Javadoc dans `target/site/apidocs/index.html` |

Le paquet `view` (affichage JavaFX) est exclu de la mesure de couverture : il est testé en jouant.

Chaque push sur GitHub lance `mvn verify` automatiquement (`.github/workflows/ci.yml`). Le rapport de couverture est téléchargeable depuis l'onglet Actions.

## Organisation du code

```
com.epitech.fnah
├── Main.java          Lanceur
├── config/            NightConfig (réglages par nuit), GameConfig (progression sauvegardée)
├── core/              Game (façade), GameLoop, GameClock, GameState, EventBus, GameEvent
├── model/             PowerSystem, Door, CameraSystem (tablette), Office (régie), Room, MapGraph (plan + plus court chemin)
├── entity/            Animatronic (base des clowns), Bobo, Dice (hasard remplaçable en test)
└── view/              GameApp (fenêtre, entrées), GameRenderer (dessin), CameraImages, AudioManager (musiques)
```

Images : `src/main/resources/images/cameras/<salle>.jpg` (1280x720), une par salle filmée, nommée d'après la constante de `Room` en minuscules (ex. `couloir_gauche.jpg`).

Règles à respecter :

- **La logique n'importe jamais JavaFX.** Tout ce qui est hors de `view/` se teste sans fenêtre.
- **L'affichage ne modifie jamais la logique directement.** Il passe par `Game` et écoute l'`EventBus`.
- **Le hasard passe toujours par `Dice`**, pour que les tests soient reproductibles.

### Patrons de conception utilisés

| Patron | Où | Pourquoi |
|---|---|---|
| Template Method | `Animatronic.update()` appelle `act()` | Minuteur et jet de dé communs, action propre à chaque clown |
| Observer | `EventBus`, `GameEvent` | La logique publie, l'affichage écoute, sans dépendance entre eux |
| Facade | `Game` | Un seul point d'entrée pour l'affichage |
| Strategy (injection) | `Dice` | Hasard réel en jeu, valeurs fixes en test |

## Ajouter un clown

1. Créer une classe dans `entity/` qui hérite d'`Animatronic` et implémente `act(GameContext)`.
2. Ajouter son niveau d'IA dans `NightConfig`.
3. L'enregistrer dans `Game` (`loop.registerSystem(...)`) et publier ses déplacements sur l'`EventBus`.
4. Écrire ses tests avec un `Dice` fixe, sur le modèle de `BoboTest`.

## Crédits

Images des caméras : source et licence à compléter avant la mise en ligne publique.

Musiques (`menu`, `nuit`) : titre, auteur et licence à compléter.
