package com.epitech.fnah.entity;

import com.epitech.fnah.core.GameContext;
import com.epitech.fnah.core.GameEvent;
import com.epitech.fnah.model.BarrelOrgan;
import com.epitech.fnah.model.Room;

/**
 * Pépin, le diable à ressort de la fosse d'orchestre.
 *
 * <p>Il ne se déplace pas : il dort dans sa boîte tant que l'orgue de barbarie
 * joue. L'orgue se vide d'autant plus vite que son niveau d'IA est haut, et le
 * joueur le remonte en tenant la manivelle depuis la caméra 8. Si l'orgue
 * s'arrête, Pépin sort par la trappe : les portes ne servent à rien contre lui.
 * Le joueur a {@value #ESCAPE_DELAY} secondes pour remonter l'orgue jusqu'à
 * {@value #RETURN_THRESHOLD} % et le renvoyer dans sa boîte, ou pour se cacher
 * sous le bureau : Pépin fouille alors la régie pendant {@value #SEARCH_TIME}
 * secondes et repart s'il ne trouve personne. Sortir de sa cachette ou faire
 * du bruit pendant la fouille est fatal.</p>
 */
public final class Pepin extends Animatronic {

    /** Nom de Pépin. */
    public static final String NAME = "Pépin";
    /** Dossier de ses images. */
    public static final String SPRITE_ID = "pepin";
    /** Intervalle du minuteur de base (inutilisé : Pépin agit en continu). */
    public static final double ACTION_INTERVAL = 1.0;
    /** Charge perdue par seconde et par niveau d'IA. */
    public static final double DRAIN_PER_LEVEL = 0.12;
    /** Secondes entre la sortie de Pépin et son attaque. */
    public static final double ESCAPE_DELAY = 5.0;
    /** Charge à atteindre pour renvoyer Pépin dans sa boîte. */
    public static final double RETURN_THRESHOLD = 30.0;
    /** Secondes que Pépin passe à fouiller la régie quand le joueur est caché. */
    public static final double SEARCH_TIME = 5.0;
    /** Bruit fait par seconde de manivelle. */
    public static final double CRANK_NOISE_PER_SECOND = 8.0;
    /** Salle de sa boîte. */
    public static final Room START_ROOM = Room.FOSSE_ORCHESTRE;

    private boolean out;
    private boolean searching;
    private double escapeTimer;
    private double searchTimer;

    /**
     * Crée Pépin dans sa boîte.
     *
     * @param baseAiLevel niveau d'IA de départ
     * @param dice        source de hasard (non utilisée, gardée pour l'uniformité)
     */
    public Pepin(int baseAiLevel, Dice dice) {
        super(NAME, START_ROOM, baseAiLevel, ACTION_INTERVAL, dice);
    }

    @Override
    protected boolean updateSpecialBehavior(double deltaSeconds, GameContext ctx) {
        BarrelOrgan organ = ctx.office.getOrgan();
        if (organ.isWinding() && ctx.office.getCameras().isWatching(START_ROOM)) {
            organ.wind(deltaSeconds);
            ctx.office.getNoise().make(CRANK_NOISE_PER_SECOND * deltaSeconds);
        } else {
            organ.drain(DRAIN_PER_LEVEL * getAiLevel(ctx.clock.getHour()) * deltaSeconds);
        }

        if (!out) {
            if (organ.isEmpty()) {
                out = true;
                escapeTimer = ESCAPE_DELAY;
                ctx.publish(new GameEvent.PepinEscaped(true));
            }
            return true;
        }
        if (organ.getCharge() >= RETURN_THRESHOLD) {
            out = false;
            ctx.publish(new GameEvent.PepinEscaped(false));
            return true;
        }
        if (searching) {
            // Pépin fouille la régie : il trouve le joueur s'il sort de sa cachette ou fait du bruit
            if (!ctx.office.isHidden() || ctx.office.getNoise().isLoud()) {
                attack(ctx);
                return true;
            }
            searchTimer -= deltaSeconds;
            if (searchTimer <= 0) {
                out = false;
                searching = false;
                organ.wind(RETURN_THRESHOLD / BarrelOrgan.WIND_RATE);
                ctx.publish(new GameEvent.PepinEscaped(false));
            }
            return true;
        }
        escapeTimer -= deltaSeconds;
        if (escapeTimer <= 0) {
            if (ctx.office.isHidden()) {
                searching = true;
                searchTimer = SEARCH_TIME;
            } else {
                attack(ctx);
            }
        }
        return true;
    }

    private void attack(GameContext ctx) {
        out = false;
        searching = false;
        moveTo(Room.REGIE);
        ctx.attackPlayer(getName());
    }

    /**
     * Pépin n'agit pas au dé : tout se passe dans
     * {@link #updateSpecialBehavior(double, GameContext)}.
     *
     * @param ctx état de la nuit
     */
    @Override
    protected void act(GameContext ctx) {
        // Rien : Pépin dépend seulement de l'orgue.
    }

    @Override
    protected void resetSpecificBehavior() {
        out = false;
        searching = false;
        searchTimer = 0;
        escapeTimer = 0;
    }

    /**
     * Indique si Pépin est sorti de sa boîte.
     *
     * @return {@code true} si l'orgue s'est arrêté et que Pépin rôde
     */
    public boolean isOut() {
        return out;
    }

    /**
     * Renvoie le temps restant avant l'attaque de Pépin, s'il est sorti.
     *
     * @return secondes restantes, ou 0 s'il est dans sa boîte
     */
    public double getEscapeTimeRemaining() {
        return out ? Math.max(0, escapeTimer) : 0;
    }

    /**
     * Indique si Pépin fouille la régie à la recherche du joueur caché.
     *
     * @return {@code true} pendant la fouille
     */
    public boolean isSearching() {
        return searching;
    }
}
