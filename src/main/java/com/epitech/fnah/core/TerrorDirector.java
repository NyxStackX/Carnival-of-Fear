package com.epitech.fnah.core;

import com.epitech.fnah.entity.Dice;
import com.epitech.fnah.model.FuseBox;

import java.util.List;
import java.util.Objects;

/**
 * Metteur en scène de la peur : il déclenche des événements inquiétants
 * (bruits, apparitions, lumières qui faiblissent, silences, surtensions)
 * à des moments imprévisibles, sans jamais répéter deux fois le même de suite.
 *
 * <p>Le rythme suit une courbe de tension : un événement est parfois suivi
 * d'un second, très vite, puis vient un calme trompeur plus long. Plus la
 * nuit avance, et plus le numéro de la nuit est élevé, plus les événements
 * sont fréquents et variés. Pendant une coupure de courant, seuls les sons
 * restent possibles. La première frayeur de chaque nuit est toujours une voix
 * (petite fille ou rire de femme). Tout le hasard passe par {@link Dice} pour les tests.</p>
 */
public final class TerrorDirector implements Updatable {

    /** Ce qui peut arriver. */
    public enum Scare {
        /** Bruits de pas, d'un côté ou de l'autre. */
        FOOTSTEPS(1, true),
        /** Une porte grince quelque part. */
        DOOR_CREAK(1, true),
        /** La tasse tombe du bureau. */
        OBJECT_FALL(1, false),
        /** La lampe de bureau grésille et s'éteint un instant. */
        LIGHT_FLICKER(1, false),
        /** L'orgue joue au ralenti, déformé, au loin. */
        WARPED_MUSIC(1, true),
        /** Une petite fille fredonne et rit, tout près. */
        LITTLE_GIRL(1, true),
        /** Silence total, puis un bruit sec. */
        SILENCE(2, false),
        /** Une silhouette attend au fond d'un couloir. */
        SHADOW(2, false),
        /** Un visage apparaît sur une caméra. */
        CAMERA_FACE(3, false),
        /** Un rire de femme, au loin, qui se déforme. */
        DISTANT_LAUGH(1, true),
        /** Surtension : un fusible saute. */
        POWER_SURGE(2, false);

        private final int firstNight;
        private final boolean audibleInDark;

        Scare(int firstNight, boolean audibleInDark) {
            this.firstNight = firstNight;
            this.audibleInDark = audibleInDark;
        }

        /**
         * Première nuit où cet événement peut arriver.
         *
         * @return numéro de nuit
         */
        public int getFirstNight() {
            return firstNight;
        }

        /**
         * Indique si l'événement peut arriver pendant une coupure de courant.
         *
         * @return {@code true} pour les sons seuls
         */
        public boolean isAudibleInDark() {
            return audibleInDark;
        }
    }

    /** Intervalle de base entre deux événements, en secondes, à la nuit 1. */
    public static final double BASE_INTERVAL = 40.0;
    /** Intervalle minimal entre deux événements, en secondes. */
    public static final double MIN_INTERVAL = 8.0;

    private final Dice dice;
    private int night = 1;
    private double timer;
    private int burst;
    private Scare last;

    /**
     * Crée le metteur en scène.
     *
     * @param dice source de hasard
     */
    public TerrorDirector(Dice dice) {
        this.dice = Objects.requireNonNull(dice, "dice");
        timer = BASE_INTERVAL;
    }

    /**
     * Règle la nuit en cours (avant le début de la nuit).
     *
     * @param night numéro de la nuit, 1 ou plus
     * @throws IllegalArgumentException si le numéro est inférieur à 1
     */
    public void setNight(int night) {
        if (night < 1) {
            throw new IllegalArgumentException("night must be >= 1");
        }
        this.night = night;
    }

    /**
     * Intervalle moyen entre deux événements à une heure donnée.
     *
     * @param hour heure de jeu (0 à 6)
     * @return secondes
     */
    public double intervalAt(int hour) {
        return Math.max(MIN_INTERVAL, BASE_INTERVAL - 6.0 * (night - 1) - 2.5 * hour);
    }

    @Override
    public void update(double deltaSeconds, GameContext ctx) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("deltaSeconds must be >= 0");
        }
        timer -= deltaSeconds;
        if (timer > 0) {
            return;
        }
        boolean dark = ctx.power.isPowerOut();
        Scare scare = pick(dark, ctx);
        double pan = (dice.rollD20() - 10.5) / 9.5;
        last = scare;
        if (scare == Scare.POWER_SURGE) {
            FuseBox.Circuit circuit = dice.rollD20() % 2 == 0 ? FuseBox.Circuit.CAMERAS : FuseBox.Circuit.LIGHTS;
            ctx.office.getFuseBox().trip(circuit);
        }
        ctx.publish(new GameEvent.ScareEvent(scare, pan));

        // Courbe de tension : parfois un second coup juste après, puis un long calme
        double base = intervalAt(ctx.clock.getHour());
        if (burst < 1 && dice.rollD20() <= 5 + night) {
            burst++;
            timer = 2.0 + dice.rollD20() * 0.2;
        } else {
            burst = 0;
            timer = base * (0.7 + dice.rollD20() * 0.04);
        }
    }

    private Scare pick(boolean dark, GameContext ctx) {
        if (last == null) {
            // Première frayeur de la nuit : toujours une voix, pour surprendre
            return dice.rollD20() % 2 == 0 ? Scare.LITTLE_GIRL : Scare.DISTANT_LAUGH;
        }
        List<Scare> allowed = java.util.Arrays.stream(Scare.values())
                .filter(s -> s.getFirstNight() <= night)
                .filter(s -> !dark || s.isAudibleInDark())
                .filter(s -> s != Scare.POWER_SURGE || ctx.office.getFuseBox().getTripped() == null)
                .toList();
        Scare scare = allowed.get(Math.floorMod(dice.rollD20(), allowed.size()));
        if (scare == last && allowed.size() > 1) {
            scare = allowed.get((allowed.indexOf(scare) + 1) % allowed.size());
        }
        return scare;
    }

    @Override
    public void reset() {
        timer = intervalAt(0) * 0.5;
        burst = 0;
        last = null;
    }

    /**
     * Secondes avant le prochain événement.
     *
     * @return temps restant
     */
    public double getTimeUntilNext() {
        return timer;
    }

    /**
     * Dernier événement déclenché.
     *
     * @return l'événement, ou {@code null} en début de nuit
     */
    public Scare getLast() {
        return last;
    }
}
