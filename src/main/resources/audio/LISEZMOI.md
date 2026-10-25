# Musiques du jeu

Déposer ici deux fichiers, lus automatiquement au lancement :

| Fichier | Quand | Volume |
|---|---|---|
| `menu.mp3` (ou `menu.wav`) | Menu principal | 60 % |
| `nuit.mp3` (ou `nuit.wav`) | Pendant la nuit | 35 % |

Les deux tournent en boucle et se coupent au jumpscare. La touche `M` coupe le son.
Si un fichier manque, le jeu reste silencieux sans planter.

Utiliser uniquement des musiques libres de droits (CC0, CC-BY...) et indiquer
l'auteur et la licence dans la section « Crédits » du README.

Sous Linux et WSL, la lecture des MP3 nécessite FFmpeg : `sudo apt install ffmpeg`.
Les fichiers `.wav` fonctionnent sans installation supplémentaire.
