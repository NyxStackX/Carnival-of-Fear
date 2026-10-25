# Sons du jeu

Les effets sonores (`sfx/`) sont générés par `tools/generate_sounds.py` (synthèse,
aucun échantillon externe) : ils sont originaux et libres de droits.
Les deux musiques ont été fournies par l'équipe et converties en WAV.

| Fichier | Rôle |
|---|---|
| `menu.wav` | Musique du menu : *Retro Electronics Prologue*, en boucle |
| `nuit.wav` | Musique de la nuit : *Serenade*, en boucle |
| `sfx/door_close.wav`, `sfx/door_open.wav` | Rideau métallique de la porte gauche |
| `sfx/tablet_up.wav`, `sfx/tablet_down.wav` | Tablette levée / baissée |
| `sfx/camera_switch.wav`, `sfx/static_loop.wav` | Changement de caméra, souffle de la tablette |
| `sfx/klaxon.wav`, `sfx/footsteps.wav` | Bobo approche / s'éloigne |
| `sfx/scream.wav` | Jumpscare |
| `sfx/hour_bell.wav`, `sfx/chime_six.wav` | Chaque heure, victoire à 6 h |
| `sfx/menu_move.wav`, `sfx/menu_select.wav`, `sfx/jack_pop.wav` | Menus, Pépin qui jaillit |
| `sfx/henry_entry.wav`, `sfx/henry_melody.wav` | Motif de Henry, sa mélodie en boucle pendant la coupure |
| `sfx/breaker.wav` | Disjoncteur qui saute (énergie à 0 %) |
| `sfx/light_buzz.wav` | Bourdonnement des lampes de couloir |
| `sfx/organ_loop.wav`, `sfx/crank.wav` | Orgue de barbarie de Pépin (CAM 8), manivelle |
| `sfx/heartbeat.wav` | Battement de cœur quand un clown est tout proche |
| `sfx/rain_loop.wav`, `sfx/thunder.wav` | Orage sur le chapiteau |
| `sfx/whisper.wav`, `sfx/child_laugh.wav` | Hallucinations (à partir de la nuit 3) |
| `sfx/typewriter.wav`, `sfx/tape_hiss.wav` | Cassette du veilleur en début de nuit |
| `sfx/honk.wav`, `sfx/crash_buzz.wav` | Easter eggs : nez de Bobo, faux plantage |

Pour changer une musique, la convertir en WAV et remplacer le fichier (le MP3 ne se lit
pas de façon fiable sous Linux) :

    ffmpeg -i ma_musique.mp3 -ac 2 -ar 24000 menu.wav

Indiquer le titre, l'auteur et la licence dans la section « Crédits » du README.
