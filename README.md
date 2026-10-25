# Minuit au Cirque Malvaux

Survival horror en Java (projet Epitech 2D GAME). Le joueur est veilleur de nuit dans un cirque abandonné et doit survivre de 00:00 à 06:00 pendant que d'anciens clowns rôdent sous le chapiteau.

## État actuel

Version jouable avec un ennemi et deux mécaniques.

- **Bobo** avance vers la porte gauche en suivant le plus court chemin dans le plan du chapiteau.
- **La porte gauche** le repousse quand elle est fermée, mais consomme de l'énergie.
- **La tablette des caméras** montre les 8 salles du chapiteau. Elle consomme de l'énergie et, tant qu'elle est levée, la porte est hors de portée.
- Une nuit dure 6 minutes réelles. Les nuits 1 à 5 rendent Bobo de plus en plus actif.
- La progression (nuits débloquées) est sauvegardée entre deux parties.
- Musique du menu (*Retro Electronics Prologue*), musique de nuit (*Serenade*) et effets sonores (porte, tablette, klaxon de Bobo qui se rapproche, pas, cri, carillon de 6 h), spatialisés à gauche ou à droite.
- Menu animé, introduction de chaque nuit, jumpscare animé, écrans de défaite et de victoire.
- **Pépin**, le diable en boîte, dort dans la fosse d'orchestre (caméra 8) et hante le menu. Sa mécanique (l'orgue de barbarie) sera la prochaine étape.

Prochaines étapes : rendre Pépin actif avec l'orgue de barbarie, lumière du couloir gauche, puis Mimi (voir le GDD).

## Lancer le jeu

Prérequis : JDK 21 et Maven.

```bash
mvn javafx:run
```

La progression est enregistrée dans `~/.cirque-malvaux/config.properties`. Supprimer ce fichier remet le jeu à la nuit 1.

## Son

Le jeu vérifie au lancement qu'une sortie audio existe. Si ce n'est pas le cas, il le signale
dans le terminal et dans le menu (« SON : INDISPONIBLE »), puis continue sans son.

### Sous WSL (Windows)

Par défaut, Linux sous WSL n'a souvent aucune sortie audio configurée : le jeu est alors muet.
Pour envoyer le son vers Windows (Windows 11, ou Windows 10 à jour avec WSLg) :

```bash
sudo apt install -y libasound2-plugins alsa-utils
cat > ~/.asoundrc <<'CONF'
pcm.!default { type pulse }
ctl.!default { type pulse }
CONF
speaker-test -t sine -f 440 -l 1   # doit faire un bip, sinon voir ci-dessous
```

Si `speaker-test` reste muet, vérifier que `echo $PULSE_SERVER` affiche un chemin, puis
redémarrer WSL depuis PowerShell avec `wsl --shutdown`. Sans WSLg, lancer plutôt le jeu
directement sous Windows (JDK 21 + Maven installés côté Windows).

### Formats

Sous Linux, JavaFX ne lit que le **WAV** sans dépendance supplémentaire (le MP3 y demande
une version de FFmpeg compatible). Toutes les musiques et tous les effets du jeu sont donc en WAV.

## Contrôles

| Action | Touche |
|---|---|
| Ouvrir / fermer la porte gauche | `Q` ou clic sur le bouton PORTE (tablette baissée) |
| Lever / baisser la tablette | `Espace` ou clic sur la barre CAMÉRAS |
| Choisir une caméra | `1` à `8`, `Gauche` / `Droite`, ou clic sur le plan |
| Naviguer dans les menus | `Haut` / `Bas`, `Entrée`, ou la souris |
| Choisir la nuit (menu) | `Gauche` / `Droite` sur la ligne NUIT |
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
└── view/              GameApp (fenêtre), ScreenManager + écrans (Menu, Night, Jumpscare, End),
                       Assets (polices, images), SoundManager (musiques et effets), Draw, OptionList
```

Ressources (`src/main/resources/`) :

| Dossier | Contenu |
|---|---|
| `images/cameras/` | Une image par salle filmée (1280x720), nommée d'après `Room` en minuscules |
| `sprites/bobo/`, `sprites/pepin/` | Poses des clowns (`idle_1` à `idle_3`, `door` ou `grin`, `scare_1`, `scare_2`) |
| `audio/` | `menu.wav`, `nuit.wav` et les effets dans `sfx/` |
| `fonts/` | Rye (titres) et Special Elite (interface), avec leurs licences |

Les effets sonores sont générés par `tools/generate_sounds.py` (Python, numpy, scipy) : `python3 tools/generate_sounds.py src/main/resources/audio`. Le script ne touche pas aux musiques `menu.wav` et `nuit.wav`.

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
| State | `ScreenManager`, `Screen` | Un écran par état du jeu, choisi automatiquement |
| Strategy (injection) | `Dice` | Hasard réel en jeu, valeurs fixes en test |

## Ajouter un clown

1. Créer une classe dans `entity/` qui hérite d'`Animatronic` et implémente `act(GameContext)`.
2. Ajouter son niveau d'IA dans `NightConfig`.
3. L'enregistrer dans `Game` (`loop.registerSystem(...)`) et publier ses déplacements sur l'`EventBus`.
4. Écrire ses tests avec un `Dice` fixe, sur le modèle de `BoboTest`.

## Crédits

- **Personnages** (Bobo, Pépin) : dessins de l'équipe.
- **Musiques** : *Retro Electronics Prologue* (menu) et *Serenade* (nuit). Auteur et licence à compléter avant la mise en ligne publique.
- **Effets sonores** : créés par synthèse avec `tools/generate_sounds.py`, originaux.
- **Polices** : [Rye](https://fonts.google.com/specimen/Rye) (Sorkin Type, SIL Open Font License 1.1) et [Special Elite](https://fonts.google.com/specimen/Special+Elite) (Astigmatic, Apache 2.0). Licences dans `src/main/resources/fonts/`.
- **Images des caméras** : source et licence à compléter avant la mise en ligne publique.
