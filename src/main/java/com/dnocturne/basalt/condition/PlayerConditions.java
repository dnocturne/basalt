package com.dnocturne.basalt.condition;

import com.dnocturne.basalt.util.TimeUtil;
import com.dnocturne.basalt.util.TimeUtil.MoonPhase;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * Factory class providing built-in conditions for Players.
 *
 * <p>All conditions are designed to be composable using
 * {@link Condition#and}, {@link Condition#or}, and {@link Condition#negate}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Player is exposed to sunlight
 * Condition<Player> sunlightExposure = PlayerConditions.isDay()
 *     .and(PlayerConditions.hasSkyAccess())
 *     .and(PlayerConditions.isClearWeather())
 *     .and(PlayerConditions.noHelmet());
 *
 * // Or use the pre-built sunlight condition
 * Condition<Player> sunlight = PlayerConditions.isExposedToSunlight();
 * }</pre>
 */
public final class PlayerConditions {

    // ============================================================
    // Cached condition instances (avoid allocations in hot paths)
    // ============================================================

    private static final Condition<Player> IS_DAY = Condition.of("isDay",
            player -> TimeUtil.isDay(player.getWorld()));
    private static final Condition<Player> IS_NIGHT = Condition.of("isNight",
            player -> TimeUtil.isNight(player.getWorld()));
    private static final Condition<Player> HAS_SKY_ACCESS = Condition.of("hasSkyAccess",
            player -> player.getLocation().getBlock().getLightFromSky() >= 15);
    private static final Condition<Player> HAS_STORM = Condition.of("hasStorm",
            player -> player.getWorld().hasStorm());
    private static final Condition<Player> HAS_HELMET = Condition.of("hasHelmet",
            player -> player.getInventory().getHelmet() != null);
    private static final Condition<Player> IS_FULL_MOON = Condition.of("isFullMoon",
            player -> TimeUtil.isFullMoon(player.getWorld()));
    private static final Condition<Player> IS_NEW_MOON = Condition.of("isNewMoon",
            player -> TimeUtil.isNewMoon(player.getWorld()));
    private static final Condition<Player> IS_THUNDERING = Condition.of("isThundering",
            player -> player.getWorld().isThundering());
    private static final Condition<Player> IS_IN_OVERWORLD = Condition.of("isInOverworld",
            player -> player.getWorld().getEnvironment() == World.Environment.NORMAL);
    private static final Condition<Player> IS_IN_NETHER = Condition.of("isInNether",
            player -> player.getWorld().getEnvironment() == World.Environment.NETHER);
    private static final Condition<Player> IS_IN_END = Condition.of("isInEnd",
            player -> player.getWorld().getEnvironment() == World.Environment.THE_END);
    private static final Condition<Player> IS_BRIGHT_MOON = Condition.of("isBrightMoon",
            player -> TimeUtil.getMoonPhaseEnum(player.getWorld()).isBright());

    private PlayerConditions() {
    }

    // ============================================================
    // Time-based conditions
    // ============================================================

    /**
     * Condition that is true during daytime.
     */
    public static @NotNull Condition<Player> isDay() {
        return IS_DAY;
    }

    /**
     * Condition that is true during nighttime.
     */
    public static @NotNull Condition<Player> isNight() {
        return IS_NIGHT;
    }

    /**
     * Condition that is true when world time is within the specified range.
     *
     * @param startTick The start of the time range (inclusive)
     * @param endTick   The end of the time range (inclusive)
     */
    public static @NotNull Condition<Player> isTimeInRange(long startTick, long endTick) {
        return Condition.of(
                "isTimeInRange(" + startTick + "-" + endTick + ")",
                player -> {
                    long time = player.getWorld().getTime();
                    if (startTick <= endTick) {
                        return time >= startTick && time <= endTick;
                    } else {
                        // Handle wrap-around (e.g., 22000-2000)
                        return time >= startTick || time <= endTick;
                    }
                }
        );
    }

    // ============================================================
    // Moon phase conditions
    // ============================================================

    /**
     * Condition that is true during a full moon.
     */
    public static @NotNull Condition<Player> isFullMoon() {
        return IS_FULL_MOON;
    }

    /**
     * Condition that is true during a new moon.
     */
    public static @NotNull Condition<Player> isNewMoon() {
        return IS_NEW_MOON;
    }

    /**
     * Condition that is true when the moon phase matches any of the specified phases.
     *
     * @param phases The moon phases to match
     */
    public static @NotNull Condition<Player> isMoonPhase(@NotNull MoonPhase... phases) {
        Set<MoonPhase> phaseSet = EnumSet.noneOf(MoonPhase.class);
        for (MoonPhase phase : phases) {
            phaseSet.add(phase);
        }
        return isMoonPhase(phaseSet);
    }

    /**
     * Condition that is true when the moon phase matches any of the specified phases.
     *
     * @param phases The moon phases to match
     */
    public static @NotNull Condition<Player> isMoonPhase(@NotNull Set<MoonPhase> phases) {
        String desc = "isMoonPhase(" + phases + ")";
        return Condition.of(desc, player -> {
            MoonPhase current = TimeUtil.getMoonPhaseEnum(player.getWorld());
            return phases.contains(current);
        });
    }

    /**
     * Condition that is true when the moon is bright (>= 50% illumination).
     * Includes: Full Moon, Waning Gibbous, First Quarter, Third Quarter, Waxing Gibbous
     */
    public static @NotNull Condition<Player> isBrightMoon() {
        return IS_BRIGHT_MOON;
    }

    /**
     * Condition that is true when the moon is dark (< 50% illumination).
     * Includes: New Moon, Waning Crescent, Waxing Crescent
     */
    public static @NotNull Condition<Player> isDarkMoon() {
        return isBrightMoon().negate();
    }

    // ============================================================
    // Environmental conditions
    // ============================================================

    /**
     * Condition that is true when the player has direct sky access (light level 15).
     */
    public static @NotNull Condition<Player> hasSkyAccess() {
        return HAS_SKY_ACCESS;
    }

    /**
     * Condition that is true when the player is underground or under cover.
     */
    public static @NotNull Condition<Player> isUnderCover() {
        return hasSkyAccess().negate();
    }

    /**
     * Condition that is true when there is a storm (rain/thunder) in the world.
     */
    public static @NotNull Condition<Player> hasStorm() {
        return HAS_STORM;
    }

    /**
     * Condition that is true when the weather is clear (no storm).
     */
    public static @NotNull Condition<Player> isClearWeather() {
        return hasStorm().negate();
    }

    /**
     * Condition that is true when there is thunder.
     */
    public static @NotNull Condition<Player> isThundering() {
        return IS_THUNDERING;
    }

    // ============================================================
    // Equipment conditions
    // ============================================================

    /**
     * Condition that is true when the player is wearing a helmet.
     */
    public static @NotNull Condition<Player> hasHelmet() {
        return HAS_HELMET;
    }

    /**
     * Condition that is true when the player is NOT wearing a helmet.
     */
    public static @NotNull Condition<Player> noHelmet() {
        return hasHelmet().negate();
    }

    // ============================================================
    // World/Environment type conditions
    // ============================================================

    /**
     * Condition that is true when the player is in the overworld.
     */
    public static @NotNull Condition<Player> isInOverworld() {
        return IS_IN_OVERWORLD;
    }

    /**
     * Condition that is true when the player is in the nether.
     */
    public static @NotNull Condition<Player> isInNether() {
        return IS_IN_NETHER;
    }

    /**
     * Condition that is true when the player is in the end.
     */
    public static @NotNull Condition<Player> isInEnd() {
        return IS_IN_END;
    }

    // ============================================================
    // Composite/common conditions
    // ============================================================

    /**
     * Condition that is true when the player is exposed to sunlight.
     * This is a composite condition checking:
     * - Daytime
     * - Has sky access (light level 15)
     * - No storm (weather protection)
     * - No helmet
     */
    public static @NotNull Condition<Player> isExposedToSunlight() {
        return isDay()
                .and(hasSkyAccess())
                .and(isClearWeather())
                .and(noHelmet());
    }

    /**
     * Condition that is true when the player is protected from sunlight.
     * Any of these provide protection:
     * - Nighttime
     * - Under cover (no sky access)
     * - Storm (weather protection)
     * - Wearing helmet
     */
    public static @NotNull Condition<Player> isProtectedFromSunlight() {
        return isExposedToSunlight().negate();
    }

    /**
     * Condition that is true during a night with a full moon.
     */
    public static @NotNull Condition<Player> isFullMoonNight() {
        return isNight().and(isFullMoon());
    }

    /**
     * Condition that is true during a night with a bright moon (>= 50% illumination).
     */
    public static @NotNull Condition<Player> isBrightMoonNight() {
        return isNight().and(isBrightMoon());
    }
}
