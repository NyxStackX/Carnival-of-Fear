# Midnight at the Malvaux Circus

Survival horror game in Java (Epitech 2D GAME project). The player is a night watchman in an abandoned circus and must survive from 00:00 to 06:00 while former clowns roam beneath the big top.

The game itself (menus, subtitles, story) and the Javadoc are in French; this README is in English.

## Features

Final version: a cinematic prologue, five story nights of 4 minutes each, a custom night and a real ending.

- **Prologue**: at launch (and from the menu, PROLOGUE), a skippable cinematic tells the 1994 fire: the last show, the burning dressing rooms, the next day's newspaper, the job offer thirty years later, and something waiting at the end of the corridor.
- **A clown in front of an open door always kills**: when Bobo reaches the left corridor, he waits a few seconds (less at higher levels). If the door is still open, he comes in and the capture sequence starts (jumpscare, static, defeat screen with replay). If it is closed, he retreats and searches elsewhere before hunting again.
- **Smarter clowns**: Bobo alternates between hunting (shortest path) and wandering, and searches after losing you. Bobo and Mimi **hear the noise** you make (doors, crank, fuse lever, wrong safe code): the louder the office, the more active they get. A noise gauge is shown on screen.
- **Hiding**: press `S` to hide under the desk. You cannot use anything while hidden, but if Pépin escapes he searches the office and leaves if you stay silent. Leaving the hiding place or making noise during his search is fatal. Hiding does not stop clowns at an open door.
- **Fuse box**: power surges can trip the camera or hallway light circuit. Hold `F` (or the lever) on the fuse box above the left door control panel to reset it; it is noisy.
- **Director's safe and clues**: each night, a 3-digit code is scrawled on the walls of three rooms (CAM 2, CAM 5, CAM 6). Found digits go into the watchman's notebook (`J`). Opening the safe (click it, under the desk on the right) gives a backup battery (+20% power) and a page of Henry's diary that tells more of the story.
- **The watchman's tapes**: each story night opens on Marcel's reel-to-reel recorder: spinning reels, VU meter, tape counter and subtitles. Halfway through, the tape glitches, a distorted voice laughs and something else whispers on screen.
- **Terror director**: footsteps and creaking doors from either side, a little girl humming, a distant woman's laugh, the organ playing on its own, the cup falling off the desk, the desk lamp failing, total silences followed by a sting, shadows at the end of a lit corridor, a face on a camera, power surges. The first scare of every night is always a voice (the little girl or the laughing woman). Events never repeat back to back, come in bursts followed by deceptive calm, and grow more frequent and varied each night and each hour.

- **Bobo** walks to the left door along the shortest path through the circus map. **Mimi** sneaks through the right side and jams the right door if nobody watches her on CAM 7. **Zig et Zag** never attack, but they scramble cameras and play fake horns.
- **Doors** (left and right) push clowns back but consume power.
- **Hallway lights**: hold `A` / `D` to light a hallway. It is the only way to see who is standing right behind a door. Lights flicker more and more as power runs low.
- **Pépin and the barrel organ**: the organ in the orchestra pit (CAM 8) slowly winds down. Hold `R` on CAM 8 to wind it. If it stops, Pépin climbs out through the trapdoor and doors are useless against him: wind it back to 30% within 5 seconds or he attacks.
- **Power outage**: at 0% everything goes dark (doors open, tablet off). After a while, Henry's eyes light up in the left doorway and he plays his melody. When it ends, he comes in. Only 06:00 can save you.
- **Atmosphere**: storm (rain, lightning that briefly lights the hallways and sometimes reveals a silhouette), heartbeat and red pulse when a threat is close, hallucinations from night 3 (the poster, the newspaper clipping, words on the cameras, whispers).
- **Custom night** (unlocked after night 5): every clown from 0 to 20, plus the 20/20/20/20 challenge.
- **Stars** shown in the menu: finish night 5, finish a night without closing a door, finish a night without cameras, finish the 20/20/20/20 challenge. They are saved with the progress.
- **Ending**: after night 5, the next morning's newspaper, then credits with the whole troupe.
- **Easter eggs**: type `HONK` in the menu, click Bobo's red nose on the office poster, and keep an eye out for a golden Bobo on the cameras (1 chance in 1000; do not stare at him).
- One night lasts 4 real-time minutes. Power drain is scaled so a night costs the same share of energy as before. Progress (unlocked nights, stars, volume) is saved between sessions.

## Running the Game

Requirements: JDK 21 and Maven.

```bash
mvn javafx:run
```

Progress is saved in `~/.cirque-malvaux/config.properties`. Deleting this file resets the game to night 1 and removes the stars.

## Sound

At startup, the game checks whether an audio output is available. If not, it reports this in the terminal and in the menu ("SOUND: UNAVAILABLE"), then continues without sound.

### Under WSL (Windows)

By default, Linux under WSL often has no audio output configured, so the game is silent.
To send sound to Windows (Windows 11, or an up-to-date Windows 10 with WSLg):

```bash
sudo apt install -y libasound2-plugins alsa-utils
cat > ~/.asoundrc <<'CONF'
pcm.!default { type pulse }
ctl.!default { type pulse }
CONF
speaker-test -t sine -f 440 -l 1   # should beep; otherwise, see below
```

If `speaker-test` remains silent, check that `echo $PULSE_SERVER` displays a path, then restart WSL from PowerShell with `wsl --shutdown`. Without WSLg, run the game directly under Windows instead (with JDK 21 + Maven installed on the Windows side).

### Formats

On Linux, JavaFX only supports **WAV** without an additional dependency (MP3 requires a compatible version of FFmpeg). All game music and sound effects are therefore in WAV format.

## Controls

| Action | Key |
|---|---|
| Open / close the left / right door | `Q` / `E`, or click the DOOR buttons (tablet lowered) |
| Light the left / right hallway (hold) | `A` / `D`, or hold the LIGHT buttons |
| Raise / lower the tablet | `Space` or click the CAMERAS bar |
| Choose a camera | `1` to `8`, `Left` / `Right`, or click the map |
| Wind the barrel organ (hold, on CAM 8) | `R`, or hold the organ panel |
| Hide under the desk / come out | `S`, or the HIDE button |
| Reset a tripped fuse (hold) | `F`, or hold the fuse box lever |
| Watchman's notebook | `J`, or click the notebook on the desk |
| Director's safe, newspaper clipping | Click them in the office; type the code with the digit keys or the keypad |
| Skip the night intro and the tape | `Enter` |
| Pause | `P` or `Esc` |
| Navigate menus | `Up` / `Down`, `Enter`, or the mouse |
| Choose the night / a clown level | `Left` / `Right` |
| Mute / unmute sound | `M` (volume: `Left` / `Right` on the SOUND row of the menu) |
| Speed up time x10 (test) | `F2` |
| Summon the golden Bobo on the current camera (demo) | `F3` |

## Quality

| Command | Result |
|---|---|
| `mvn test` | Runs the tests and generates the coverage report at `target/site/jacoco/index.html` |
| `mvn verify` | Same as `mvn test`, and fails if logic-code coverage drops below 80% |
| `mvn javadoc:javadoc` | Generates the Javadoc at `target/site/apidocs/index.html` |

The project has 277 unit tests (JUnit 5). The `view` package (JavaFX display) is excluded from coverage measurement: it is tested by playing the game.

Every push to GitHub automatically runs `mvn verify` (`.github/workflows/ci.yml`). The coverage report can be downloaded from the Actions tab.

## Code Structure

```
com.epitech.fnah
├── Main.java          Launcher
├── config/            NightConfig (settings per night), CustomNight, Achievement (stars), GameConfig (saved progress)
├── core/              Game (facade), GameLoop, GameClock, GameState, EventBus, GameEvent,
│                      AchievementTracker (stars), SecretCode (typed easter eggs), TerrorDirector (scares)
├── model/             PowerSystem, Door, Light, CameraSystem (tablet), BarrelOrgan, Office (control room),
│                      NoiseMeter, FuseBox, Safe, Room, MapGraph (map + shortest path)
├── entity/            Animatronic (clown base class), Bobo, Mimi, ZigEtZag, Pepin, Henry (power outage),
│                      Dice (randomness replaceable in tests)
└── view/              GameApp (window), ScreenManager + screens (Intro, Menu, Night, Jumpscare, End, Ending),
                       DeskOverlays (safe, notebook, clipping),
                       Assets (fonts, images), SoundManager (music and effects), Draw, Ui (shared interface
                       components: panels, buttons, keycaps, gauges, marquee bulbs), OptionList
```

Resources (`src/main/resources/`):

| Folder | Contents |
|---|---|
| `images/cameras/` | One image per monitored room (1280x720), named after `Room` in lowercase |
| `sprites/<clown>/` | Clown poses for Bobo, Mimi, Zig et Zag, Pépin and Henry (`idle_1` to `idle_3`, `door` or `grin`, `scare_1` to `scare_3`) |
| `audio/` | `menu.wav`, `nuit.wav`, and effects in `sfx/` |
| `fonts/` | Eater (titles), IM Fell English SC, Rock Salt and Special Elite (interface), with their licenses |

Sound effects are generated by `tools/generate_sounds.py` (Python, numpy, scipy): `python3 tools/generate_sounds.py src/main/resources/audio`. The script does not modify the `menu.wav` and `nuit.wav` music files.

Images: `src/main/resources/images/cameras/<room>.jpg` (1280x720), one per monitored room, named after the `Room` constant in lowercase (e.g. `couloir_gauche.jpg` for `Room.COULOIR_GAUCHE`).

Rules to follow:

- **The logic never imports JavaFX.** Everything outside `view/` can be tested without a window.
- **The display never modifies the logic directly.** It goes through `Game` and listens to the `EventBus`.
- **Randomness always goes through `Dice`**, so tests are reproducible.

### Design Patterns Used

| Pattern | Where | Why |
|---|---|---|
| Template Method | `Animatronic.update()` calls `act()` | Shared timer and dice roll, with an action specific to each clown |
| Observer | `EventBus`, `GameEvent`, `AchievementTracker` | The logic publishes events; the display, the sound and the star tracker listen without depending on each other |
| Facade | `Game` | A single entry point for the display |
| State | `ScreenManager`, `Screen` | One screen per game state, selected automatically |
| Strategy (injection) | `Dice` | Real randomness in the game, fixed values in tests |
| Template Method (variant) | `Pepin`, `Mimi`, `ZigEtZag` override `updateSpecialBehavior()` | Clowns with continuous behaviour (organ, door jam, fleeing) reuse the same update cycle |

## Adding a Clown

1. Create a class in `entity/` that extends `Animatronic` and implements `act(GameContext)`.
2. Add its AI level to `NightConfig`.
3. Register it in `Game` (`loop.registerSystem(...)`, or `loop.registerBlackoutSystem(...)` for something that only acts during a power outage) and publish its movements through the `EventBus`.
4. Write its tests using a fixed `Dice`, following the `BoboTest` model.

## Credits

- **Characters** (Bobo, Mimi, Zig et Zag, Pépin, Henry): illustrations by the team.
- **Music**: *Retro Electronics Prologue* (menu) and *Serenade* (night). Author and license details must be completed before public release.
- **Sound effects** (including the storm, heartbeat, barrel organ, Henry's melody, tape, creaking doors, fire, wind and easter eggs): generated through synthesis with `tools/generate_sounds.py`; original.
- **Voices** (`sfx/girl_1` to `girl_4`, `sfx/laugh_1` to `laugh_4`): cut from the two horror sound files provided by the team (*Creepy little girl*, *Evil laughing woman*). Source and license details must be completed before public release.
- **Fonts**: [Eater](https://fonts.google.com/specimen/Eater) (Typomondo, SIL Open Font License 1.1) for titles, [IM Fell English SC](https://fonts.google.com/specimen/IM+Fell+English+SC) (Igino Marini, OFL 1.1), [Rock Salt](https://fonts.google.com/specimen/Rock+Salt) (Font Diner, Apache 2.0) for wall scrawls, and [Special Elite](https://fonts.google.com/specimen/Special+Elite) (Astigmatic, Apache 2.0). Licenses are in `src/main/resources/fonts/`.
- **Camera images**: source and license details must be completed before public release.
