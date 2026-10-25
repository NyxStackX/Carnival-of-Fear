#!/usr/bin/env bash
# Diagnostic du son pour Minuit au Cirque Malvaux (Linux et WSL).
ok()   { printf '  [OK]     %s\n' "$1"; }
ko()   { printf '  [ECHEC]  %s\n' "$1"; }
info() { printf '  [INFO]   %s\n' "$1"; }
SFX="src/main/resources/audio/sfx/klaxon.wav"

echo "1. Système"
if grep -qi microsoft /proc/version 2>/dev/null; then info "WSL détecté ($(uname -r))"; WSL=1; else info "Linux classique ($(uname -r))"; WSL=0; fi

echo "2. Serveur de son"
if [ "$WSL" = 1 ]; then
    if [ -S /mnt/wslg/PulseServer ]; then ok "WSLg fournit un serveur de son"; else ko "pas de serveur de son WSLg (wsl --update dans PowerShell)"; fi
    if [ -n "$PULSE_SERVER" ]; then ok "PULSE_SERVER = $PULSE_SERVER"; else ko "PULSE_SERVER vide (wsl --shutdown dans PowerShell puis rouvrir le terminal)"; fi
fi

echo "3. Pont ALSA vers le serveur de son"
if dpkg -s libasound2-plugins >/dev/null 2>&1; then ok "libasound2-plugins installé"; else ko "libasound2-plugins manquant"; fi
if [ -f "$HOME/.asoundrc" ] && grep -q "type pulse" "$HOME/.asoundrc"; then ok "~/.asoundrc envoie le son vers pulse"; else ko "~/.asoundrc absent ou incomplet"; fi

echo "4. Lecture d'un son du jeu par ALSA"
if ! command -v aplay >/dev/null; then ko "aplay manquant (sudo apt install -y alsa-utils)"
elif [ ! -f "$SFX" ]; then ko "$SFX introuvable"
elif OUT=$(aplay -q "$SFX" 2>&1); then ok "aplay a joué le klaxon (entendu ?)"
else ko "aplay a échoué : $OUT"; fi

echo "5. Sortie audio vue par Java"
TMP=$(mktemp -d)
cat > "$TMP/CheckAudio.java" <<'JAVA'
import javax.sound.sampled.*;
public class CheckAudio {
    public static void main(String[] a) {
        AudioFormat f = new AudioFormat(44100, 16, 2, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(f)) {
            line.open(f); line.start();
            byte[] b = new byte[88200];
            for (int i = 0; i < b.length / 4; i++) {
                short s = (short) (Math.sin(2 * Math.PI * 440 * i / 44100.0) * 8000);
                b[4*i] = (byte) s; b[4*i+1] = (byte) (s >> 8); b[4*i+2] = (byte) s; b[4*i+3] = (byte) (s >> 8);
            }
            line.write(b, 0, b.length); line.drain();
            System.out.println("OK");
        } catch (Exception e) { System.out.println("ECHEC " + e); }
    }
}
JAVA
JOUT=$(java "$TMP/CheckAudio.java" 2>&1 | grep -v JAVA_TOOL | tail -1)
case "$JOUT" in OK*) ok "Java a joué un bip (entendu ?)";; *) ko "Java ne trouve aucune sortie audio : $JOUT";; esac
rm -rf "$TMP"
