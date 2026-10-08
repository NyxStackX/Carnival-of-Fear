# Minuit au Cirque Malvaux

Survival horror en Java (projet Epitech 2D GAME). Le joueur est veilleur de nuit dans un cirque abandonné et doit survivre de 00:00 à 06:00 pendant que d'anciens clowns rôdent sous le chapiteau.

## État actuel

Première version jouable : un ennemi, une mécanique.

- **Bobo** avance vers la porte gauche en suivant le plus court chemin dans le plan du chapiteau.
- **La porte gauche** le repousse quand elle est fermée, mais consomme de l'énergie.
- Une nuit dure 6 minutes réelles. Les nuits 1 à 5 rendent Bobo de plus en plus actif.
- La progression (nuits débloquées) est sauvegardée entre deux parties.

Prochaines étapes : lumière de couloir, caméras, puis Mimi (voir le GDD).

## Lancer le jeu

Prérequis : JDK 21 et Maven.

```bash
mvn javafx:run
```

La progression est enregistrée dans `~/.cirque-malvaux/config.properties`. Supprimer ce fichier remet le jeu à la nuit 1.

## Contrôles

| Action | Touche |
|---|---|
| Ouvrir / fermer la porte gauche | `Q` ou clic sur le bouton PORTE |
| Choisir la nuit (menu) | `Gauche` / `Droite` |
| Commencer, rejouer, nuit suivante | `Entrée` |
| Retour au menu | `Échap` |
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
├── model/             PowerSystem, Door, Office (régie), Room, MapGraph (plan + plus court chemin)
├── entity/            Animatronic (base des clowns), Bobo, Dice (hasard remplaçable en test)
└── view/              GameApp (fenêtre, entrées), GameRenderer (dessin)
```

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
