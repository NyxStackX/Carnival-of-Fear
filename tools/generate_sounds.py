"""Génère les musiques et effets sonores du jeu par synthèse.

Tous les sons sont créés ici à partir de zéro (aucun échantillon externe) :
ils sont donc originaux et libres de droits.

Utilisation (depuis la racine du projet) :
    python3 tools/generate_sounds.py src/main/resources/audio           # effets seulement
    python3 tools/generate_sounds.py src/main/resources/audio --music   # + musiques de remplacement

Sans --music, menu.wav et nuit.wav (les musiques choisies par l'équipe) ne sont pas touchés.

Dépendances : numpy, scipy.
"""

import sys
from pathlib import Path

import numpy as np
from scipy.io import wavfile
from scipy.signal import butter, lfilter, sosfilt

RATE = 22050
rng = np.random.default_rng(1994)


# ---------------------------------------------------------------- outils

def t_axis(seconds):
    return np.arange(int(seconds * RATE)) / RATE


def env_adsr(n, attack=0.005, decay=0.1, sustain=0.0, release=0.05):
    """Enveloppe simple, en secondes."""
    a, d, r = int(attack * RATE), int(decay * RATE), int(release * RATE)
    s = max(0, n - a - d - r)
    e = np.concatenate([
        np.linspace(0, 1, max(a, 1)),
        np.linspace(1, sustain, max(d, 1)),
        np.full(s, sustain),
        np.linspace(sustain, 0, max(r, 1)),
    ])
    return np.pad(e, (0, max(0, n - len(e))))[:n]


def exp_decay(n, seconds):
    return np.exp(-np.arange(n) / (seconds * RATE))


def lowpass(x, freq, order=2):
    return sosfilt(butter(order, freq, 'low', fs=RATE, output='sos'), x)


def highpass(x, freq, order=2):
    return sosfilt(butter(order, freq, 'high', fs=RATE, output='sos'), x)


def bandpass(x, lo, hi, order=2):
    return sosfilt(butter(order, [lo, hi], 'band', fs=RATE, output='sos'), x)


def noise(seconds):
    return rng.uniform(-1, 1, int(seconds * RATE))


def reverb(x, wet=0.35, size=1.0):
    """Réverbération de Schroeder : 4 filtres en peigne + 2 passe-tout."""
    out = np.zeros(len(x))
    for delay_ms, gain in [(29.7, 0.80), (37.1, 0.78), (41.1, 0.76), (43.7, 0.74)]:
        d = int(delay_ms * size * RATE / 1000)
        a = np.zeros(d + 1)
        a[0], a[d] = 1, -gain
        out += lfilter([1], a, x)
    out /= 4
    for delay_ms, gain in [(5.0, 0.7), (1.7, 0.7)]:
        d = int(delay_ms * RATE / 1000)
        b = np.zeros(d + 1)
        a = np.zeros(d + 1)
        b[0], b[d] = -gain, 1
        a[0], a[d] = 1, -gain
        out = lfilter(b, a, out)
    return (1 - wet) * x + wet * out


def mix_at(dst, src, start_s, gain=1.0):
    i = int(start_s * RATE)
    end = min(len(dst), i + len(src))
    if i < end:
        dst[i:end] += gain * src[:end - i]


def loopable(x, fade_s=1.5):
    """Rend une boucle sans coupure : la fin se fond dans le début."""
    f = int(fade_s * RATE)
    head, body, tail = x[:f], x[f:-f], x[-f:]
    ramp = np.linspace(0, 1, f)
    return np.concatenate([tail * (1 - ramp) + head * ramp, body])


def normalize(x, peak=0.9):
    m = np.max(np.abs(x))
    return x if m == 0 else x * (peak / m)


def save(folder, name, x, peak=0.9):
    path = folder / f'{name}.wav'
    path.parent.mkdir(parents=True, exist_ok=True)
    wavfile.write(path, RATE, (normalize(x, peak) * 32767).astype(np.int16))
    print(f'{path}  {len(x) / RATE:.1f} s')


def note_freq(name):
    """'A4' -> 440 Hz, 'C#5' -> ..."""
    names = {'C': -9, 'C#': -8, 'D': -7, 'D#': -6, 'E': -5, 'F': -4, 'F#': -3,
             'G': -2, 'G#': -1, 'A': 0, 'A#': 1, 'B': 2}
    pitch, octave = name[:-1], int(name[-1])
    return 440.0 * 2 ** ((names[pitch] + 12 * (octave - 4)) / 12)


# ---------------------------------------------------------------- instruments

def music_box(freq, seconds=1.6, detune=0.0):
    """Lame de boîte à musique : partiels inharmoniques, attaque sèche."""
    t = t_axis(seconds)
    f = freq * (1 + detune)
    tone = (np.sin(2 * np.pi * f * t)
            + 0.35 * np.sin(2 * np.pi * f * 2.76 * t) * exp_decay(len(t), 0.25)
            + 0.15 * np.sin(2 * np.pi * f * 5.40 * t) * exp_decay(len(t), 0.08))
    return tone * exp_decay(len(t), 0.55) * env_adsr(len(t), 0.002, 0.0, 1.0, 0.05)


def bell(freq, seconds=4.0):
    """Cloche : partiels de cloche d'église, longue résonance."""
    t = t_axis(seconds)
    out = np.zeros(len(t))
    for ratio, amp, decay in [(0.5, 0.6, 2.5), (1.0, 1.0, 1.8), (1.19, 0.5, 1.2),
                              (1.56, 0.35, 0.9), (2.0, 0.3, 0.7), (2.51, 0.2, 0.4)]:
        out += amp * np.sin(2 * np.pi * freq * ratio * t) * exp_decay(len(t), decay)
    return out * env_adsr(len(t), 0.003, 0.0, 1.0, 0.2)


def drone(freq, seconds, cutoff=300):
    t = t_axis(seconds)
    saw = sum(np.sin(2 * np.pi * freq * k * (1 + d) * t) / k
              for k in range(1, 8) for d in (-0.003, 0.003))
    return lowpass(saw, cutoff)


# ---------------------------------------------------------------- musiques

def menu_music():
    """Valse de boîte à musique en la mineur, légèrement désaccordée, avec un bourdon grave."""
    bpm, beats_per_bar = 78, 3
    beat = 60 / bpm
    # Mélodie originale : (note, durée en temps)
    melody = [
        ('E5', 1), ('A5', 1), ('C6', 1), ('B5', 2), ('A5', 1),
        ('G#5', 1), ('B5', 1), ('E5', 1), ('A5', 3),
        ('E5', 1), ('A5', 1), ('C6', 1), ('D6', 2), ('C6', 1),
        ('B5', 1), ('G#5', 1), ('B5', 1), ('A5', 3),
        ('C6', 1), ('B5', 1), ('A5', 1), ('F5', 2), ('A5', 1),
        ('E5', 1), ('D#5', 1), ('E5', 1), ('B4', 3),
        ('C5', 1), ('E5', 1), ('A5', 1), ('G#5', 2), ('B5', 1),
        ('E5', 3), ('A4', 3),
    ]
    bass = ['A3', 'E3', 'A3', 'E3', 'F3', 'E3', 'A3', 'E3', 'A3', 'E3']
    total_beats = sum(d for _, d in melody)
    seconds = total_beats * beat + 2.5
    out = np.zeros(int(seconds * RATE))

    pos = 0.0
    for name, dur in melody:
        wobble = rng.uniform(-0.004, 0.004)
        mix_at(out, music_box(note_freq(name), 2.2, wobble), pos, 0.55)
        pos += dur * beat
    for bar in range(int(total_beats // beats_per_bar)):
        root = bass[bar % len(bass)]
        mix_at(out, music_box(note_freq(root), 2.5), bar * beats_per_bar * beat, 0.35)
        mix_at(out, music_box(note_freq(root) * 1.5, 1.5), (bar * beats_per_bar + 1) * beat, 0.12)
        mix_at(out, music_box(note_freq(root) * 1.5, 1.5), (bar * beats_per_bar + 2) * beat, 0.12)

    # Bande magnétique fatiguée : léger pleurage de la hauteur
    t = t_axis(len(out) / RATE)
    warp = t + 0.0025 * np.sin(2 * np.pi * 0.4 * t)
    out = np.interp(warp * RATE, np.arange(len(out)), out)

    out = reverb(out, wet=0.45, size=1.6)
    out += 0.18 * drone(note_freq('A1'), len(out) / RATE, 160) * (0.7 + 0.3 * np.sin(2 * np.pi * 0.07 * t))
    crackle = (rng.random(len(out)) > 0.9993) * rng.uniform(-1, 1, len(out))
    out += 0.25 * highpass(crackle, 2000) + 0.01 * highpass(noise(len(out) / RATE), 3000)
    return loopable(out, 2.0)


def night_ambience():
    """Chapiteau vide la nuit : bourdon grave, vent, grincements lointains."""
    seconds = 48
    t = t_axis(seconds)
    out = 0.5 * drone(note_freq('A1'), seconds, 120) * (0.6 + 0.4 * np.sin(2 * np.pi * 0.05 * t))
    out += 0.3 * drone(note_freq('D#2'), seconds, 100) * (0.5 + 0.5 * np.sin(2 * np.pi * 0.031 * t + 1))

    wind = bandpass(noise(seconds), 200, 900)
    out += 0.35 * wind * (0.4 + 0.6 * (0.5 + 0.5 * np.sin(2 * np.pi * 0.09 * t)) ** 2)

    for start in (7, 19, 31, 42):
        creak_t = t_axis(1.6)
        f = 300 + 180 * np.sin(2 * np.pi * 0.7 * creak_t)
        phase = 2 * np.pi * np.cumsum(f) / RATE
        creak = np.sign(np.sin(phase)) * (rng.random(len(creak_t)) > 0.4)
        creak = bandpass(creak, 400, 1800) * env_adsr(len(creak_t), 0.3, 0.6, 0.4, 0.6)
        mix_at(out, reverb(creak, 0.6, 2.0), start, 0.25)

    for start, name in ((12, 'E5'), (12.9, 'A4'), (35, 'C5'), (35.9, 'B4')):
        mix_at(out, reverb(music_box(note_freq(name) * 0.5, 3.0), 0.8, 2.5), start, 0.12)

    return loopable(lowpass(out, 3000), 3.0)


# ---------------------------------------------------------------- effets

def door_close():
    n = int(1.1 * RATE)
    t = t_axis(1.1)
    hit = lowpass(noise(1.1), 2500) * exp_decay(n, 0.06)
    metal = sum(a * np.sin(2 * np.pi * f * t) * exp_decay(n, d)
                for f, a, d in [(180, 1.0, 0.35), (412, 0.6, 0.25), (733, 0.4, 0.18), (1291, 0.25, 0.1)])
    thud = np.sin(2 * np.pi * 55 * t) * exp_decay(n, 0.15)
    return reverb(hit + 0.5 * metal + 1.2 * thud, 0.25)


def door_open():
    n = int(0.8 * RATE)
    t = t_axis(0.8)
    rattle = bandpass(noise(0.8), 300, 2500) * (0.5 + 0.5 * np.sign(np.sin(2 * np.pi * 28 * t)))
    rattle *= env_adsr(n, 0.05, 0.1, 0.8, 0.2)
    clank_t = t_axis(0.4)
    clank = sum(a * np.sin(2 * np.pi * f * clank_t) for f, a in [(520, 1), (1170, 0.5)]) * exp_decay(len(clank_t), 0.07)
    out = 0.6 * rattle
    mix_at(out, clank, 0.6, 0.6)
    return reverb(out, 0.2)


def tablet_up():
    n = int(0.45 * RATE)
    t = t_axis(0.45)
    whoosh = bandpass(noise(0.45), 400, 3000) * env_adsr(n, 0.12, 0.25, 0.0, 0.05)
    blip = np.sin(2 * np.pi * 1250 * t) * env_adsr(n, 0.002, 0.08, 0.0, 0.01)
    hum = np.sin(2 * np.pi * 60 * t) * 0.3 * env_adsr(n, 0.05, 0.3, 0.0, 0.05)
    out = 0.6 * whoosh + hum
    mix_at(out, blip, 0.15, 0.35)
    return out


def tablet_down():
    n = int(0.35 * RATE)
    whoosh = bandpass(noise(0.35), 300, 2000) * env_adsr(n, 0.02, 0.25, 0.0, 0.05)
    click = highpass(noise(0.02), 2000) * exp_decay(int(0.02 * RATE), 0.004)
    out = 0.5 * whoosh
    mix_at(out, click, 0.28, 0.8)
    return out


def camera_switch():
    n = int(0.28 * RATE)
    stat = highpass(noise(0.28), 800) * env_adsr(n, 0.002, 0.2, 0.0, 0.05)
    click = noise(0.01) * exp_decay(int(0.01 * RATE), 0.002)
    out = 0.6 * stat
    mix_at(out, click, 0.0, 1.0)
    return out


def static_loop():
    s = bandpass(noise(3.0), 1000, 6000) * 0.4 + 0.1 * lowpass(noise(3.0), 400)
    return loopable(s, 0.4)


def klaxon():
    """Klaxon de clown, deux coups, un peu désaccordé et sinistre."""
    out = np.zeros(int(1.0 * RATE))
    for start, f0 in ((0.0, 410), (0.42, 380)):
        t = t_axis(0.34)
        f = f0 * (1 - 0.08 * t / t[-1])
        phase = 2 * np.pi * np.cumsum(f) / RATE
        saw = 2 * ((phase / (2 * np.pi)) % 1) - 1
        nasal = bandpass(saw, 600, 1600) + 0.5 * bandpass(saw, 2200, 3200)
        nasal = np.tanh(3 * nasal) * env_adsr(len(t), 0.01, 0.05, 0.8, 0.08)
        mix_at(out, nasal, start, 1.0)
    return reverb(out, 0.35, 1.5)


def footsteps():
    out = np.zeros(int(2.6 * RATE))
    for i, start in enumerate((0.0, 0.62, 1.24, 1.86)):
        n = int(0.4 * RATE)
        t = t_axis(0.4)
        step = (np.sin(2 * np.pi * 70 * t) + 0.4 * lowpass(noise(0.4), 600)) * exp_decay(n, 0.07)
        mix_at(out, lowpass(step, 900 - i * 180), start, 1.0 - i * 0.22)
    return reverb(out, 0.45, 1.8)


def scream():
    """Jumpscare : cri strident saturé, grondement, larsen."""
    seconds = 1.6
    n = int(seconds * RATE)
    t = t_axis(seconds)
    out = np.zeros(n)
    for ratio in (1.0, 1.06, 1.49, 2.01):
        f = (700 + 900 * (t / seconds) ** 0.5) * ratio
        phase = 2 * np.pi * np.cumsum(f * (1 + 0.03 * np.sin(2 * np.pi * 9 * t))) / RATE
        out += 2 * ((phase / (2 * np.pi)) % 1) - 1
    out += 2.0 * bandpass(noise(seconds), 300, 5000)
    out += 2.5 * np.sin(2 * np.pi * 45 * t) * exp_decay(n, 0.4)
    out = np.tanh(2.5 * out) * env_adsr(n, 0.005, 0.1, 0.9, 0.35)
    return out


def chime_six():
    out = np.zeros(int(5.5 * RATE))
    for i, name in enumerate(('E5', 'C5', 'D5', 'G4')):
        mix_at(out, bell(note_freq(name)), i * 0.55, 0.8)
    mix_at(out, bell(note_freq('C4'), 4.5), 2.4, 1.0)
    return reverb(out, 0.4, 1.8)


def hour_bell():
    return reverb(lowpass(bell(note_freq('A2'), 4.0), 1500), 0.6, 2.2)


def menu_move():
    return music_box(note_freq('E6'), 0.35)


def menu_select():
    out = np.zeros(int(1.0 * RATE))
    mix_at(out, music_box(note_freq('A5'), 0.9), 0.0, 0.7)
    mix_at(out, music_box(note_freq('E5'), 0.9), 0.0, 0.5)
    mix_at(out, door_close()[:int(0.5 * RATE)], 0.0, 0.25)
    return reverb(out, 0.3)


def jack_pop():
    """Diable qui sort de sa boîte : déclic, ressort qui vibre."""
    seconds = 0.9
    n = int(seconds * RATE)
    t = t_axis(seconds)
    f = 220 + 160 * np.sin(2 * np.pi * 14 * t) * exp_decay(n, 0.25)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    spring = np.sin(phase) * exp_decay(n, 0.3)
    click = highpass(noise(0.015), 1500)
    out = 0.7 * spring
    mix_at(out, click, 0.0, 1.0)
    return reverb(out, 0.3)


# ---------------------------------------------------------------- main

def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    root = Path(args[0] if args else 'src/main/resources/audio')
    if '--music' in sys.argv:
        # Musiques de remplacement : n'écrase menu.wav et nuit.wav que sur demande
        save(root, 'menu', menu_music(), 0.8)
        save(root, 'nuit', night_ambience(), 0.7)
    sfx = root / 'sfx'
    for name, fn, peak in [
        ('door_close', door_close, 0.9), ('door_open', door_open, 0.7),
        ('tablet_up', tablet_up, 0.6), ('tablet_down', tablet_down, 0.6),
        ('camera_switch', camera_switch, 0.5), ('static_loop', static_loop, 0.35),
        ('klaxon', klaxon, 0.85), ('footsteps', footsteps, 0.8),
        ('scream', scream, 0.98), ('chime_six', chime_six, 0.85),
        ('hour_bell', hour_bell, 0.6), ('menu_move', menu_move, 0.5),
        ('menu_select', menu_select, 0.7), ('jack_pop', jack_pop, 0.8),
    ]:
        save(sfx, name, fn(), peak)


if __name__ == '__main__':
    main()
