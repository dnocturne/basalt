package com.dnocturne.basalt.util;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for time-related calculations.
 */
public final class TimeUtil {

    // Minecraft time constants (in ticks)
    public static final long TICKS_PER_SECOND = 20L;
    public static final long TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
    public static final long TICKS_PER_HOUR = TICKS_PER_MINUTE * 60;
    public static final long TICKS_PER_DAY = 24000L;

    // Time of day boundaries
    public static final long SUNRISE = 0L;
    public static final long NOON = 6000L;
    public static final long SUNSET = 12000L;
    public static final long MIDNIGHT = 18000L;
    public static final long NIGHT_START = 13000L;
    public static final long NIGHT_END = 23000L;

    private TimeUtil() {
    }

    /**
     * Check if it's currently night time in the world.
     */
    public static boolean isNight(@NotNull World world) {
        long time = world.getTime();
        return time >= NIGHT_START && time <= NIGHT_END;
    }

    /**
     * Check if it's currently day time in the world.
     */
    public static boolean isDay(@NotNull World world) {
        return !isNight(world);
    }

    /**
     * Get the current day number (0-indexed).
     */
    public static long getDayNumber(@NotNull World world) {
        return world.getFullTime() / TICKS_PER_DAY;
    }

    /**
     * Get the current moon phase.
     * <p>
     * Formula: phase = day mod 8
     * <p>
     * Phases (in chronological order):
     * <ul>
     *   <li>0 = Full Moon</li>
     *   <li>1 = Waning Gibbous</li>
     *   <li>2 = Third Quarter</li>
     *   <li>3 = Waning Crescent</li>
     *   <li>4 = New Moon</li>
     *   <li>5 = Waxing Crescent</li>
     *   <li>6 = First Quarter</li>
     *   <li>7 = Waxing Gibbous</li>
     * </ul>
     *
     * @param world The world to check
     * @return Moon phase (0-7)
     * @see <a href="https://minecraft.wiki/w/Moon">Minecraft Wiki - Moon</a>
     */
    public static int getMoonPhase(@NotNull World world) {
        return (int) (getDayNumber(world) % 8);
    }

    /**
     * Get the moon phase as an enum.
     */
    public static @NotNull MoonPhase getMoonPhaseEnum(@NotNull World world) {
        return MoonPhase.fromPhase(getMoonPhase(world));
    }

    /**
     * Check if it's a full moon (phase 0).
     */
    public static boolean isFullMoon(@NotNull World world) {
        return getMoonPhase(world) == MoonPhase.FULL_MOON.getPhase();
    }

    /**
     * Check if it's a new moon (phase 4).
     */
    public static boolean isNewMoon(@NotNull World world) {
        return getMoonPhase(world) == MoonPhase.NEW_MOON.getPhase();
    }

    /**
     * Convert ticks to seconds.
     */
    public static long ticksToSeconds(long ticks) {
        return ticks / TICKS_PER_SECOND;
    }

    /**
     * Convert seconds to ticks.
     */
    public static long secondsToTicks(long seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * Format ticks as a human-readable duration.
     */
    public static @NotNull String formatDuration(long ticks) {
        long totalSeconds = ticksToSeconds(ticks);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Moon phases in Minecraft.
     * One complete lunar cycle takes 8 in-game days (~2h 40m real time).
     */
    public enum MoonPhase {
        FULL_MOON(0, "Full Moon", "🌕"),
        WANING_GIBBOUS(1, "Waning Gibbous", "🌖"),
        THIRD_QUARTER(2, "Third Quarter", "🌗"),
        WANING_CRESCENT(3, "Waning Crescent", "🌘"),
        NEW_MOON(4, "New Moon", "🌑"),
        WAXING_CRESCENT(5, "Waxing Crescent", "🌒"),
        FIRST_QUARTER(6, "First Quarter", "🌓"),
        WAXING_GIBBOUS(7, "Waxing Gibbous", "🌔");

        /**
         * Pre-computed lookup array for O(1) phase lookups.
         * Avoids values() iteration and array allocation on each call.
         */
        private static final MoonPhase[] BY_PHASE = new MoonPhase[8];

        static {
            for (MoonPhase mp : values()) {
                BY_PHASE[mp.phase] = mp;
            }
        }

        private final int phase;
        private final String displayName;
        private final String symbol;

        MoonPhase(int phase, String displayName, String symbol) {
            this.phase = phase;
            this.displayName = displayName;
            this.symbol = symbol;
        }

        public int getPhase() {
            return phase;
        }

        public @NotNull String getDisplayName() {
            return displayName;
        }

        public @NotNull String getSymbol() {
            return symbol;
        }

        /**
         * Get the moon brightness (0.0 to 1.0).
         * Full moon = 1.0, New moon = 0.0
         */
        public float getBrightness() {
            return switch (this) {
                case FULL_MOON -> 1.0f;
                case WANING_GIBBOUS, WAXING_GIBBOUS -> 0.75f;
                case THIRD_QUARTER, FIRST_QUARTER -> 0.5f;
                case WANING_CRESCENT, WAXING_CRESCENT -> 0.25f;
                case NEW_MOON -> 0.0f;
            };
        }

        /**
         * Check if this is a "bright" moon phase (>= 50% illumination).
         */
        public boolean isBright() {
            return getBrightness() >= 0.5f;
        }

        /**
         * Get MoonPhase from phase number using O(1) array lookup.
         *
         * @param phase The phase number (0-7)
         * @return The corresponding MoonPhase, or FULL_MOON if out of range
         */
        public static @NotNull MoonPhase fromPhase(int phase) {
            if (phase < 0 || phase >= BY_PHASE.length) {
                return FULL_MOON; // Default fallback
            }
            return BY_PHASE[phase];
        }

        /**
         * Get the locale key for this moon phase (e.g., "full-moon", "waning-gibbous").
         * Used for looking up localized names and symbols.
         */
        public @NotNull String getLocaleKey() {
            return name().toLowerCase().replace('_', '-');
        }
    }
}
