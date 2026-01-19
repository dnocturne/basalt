package com.dnocturne.basalt.component.trigger;

import com.dnocturne.basalt.component.StatefulComponent;
import com.dnocturne.basalt.condition.Condition;
import com.dnocturne.basalt.condition.PlayerConditions;
import com.dnocturne.basalt.util.TimeUtil;
import com.dnocturne.basalt.util.TimeUtil.MoonPhase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Trigger based on moon phase.
 * Useful for werewolf transformations, moon-based effects, etc.
 *
 * <p>Uses the Condition system for reusable moon phase checks.</p>
 *
 * @param <I> The instance type containing state data
 */
public class MoonPhaseTrigger<I> implements StatefulComponent<Player, I> {

    private final String id;
    private final Set<MoonPhase> activePhases;
    private final Condition<Player> condition;

    /**
     * Create a trigger for specific moon phases.
     *
     * @param id           Component ID
     * @param activePhases Set of moon phases to trigger on
     */
    public MoonPhaseTrigger(@NotNull String id, @NotNull Set<MoonPhase> activePhases) {
        this.id = id;
        this.activePhases = EnumSet.copyOf(activePhases);
        this.condition = PlayerConditions.isMoonPhase(this.activePhases);
    }

    /**
     * Create a trigger for specific moon phases (varargs).
     */
    public MoonPhaseTrigger(@NotNull String id, @NotNull MoonPhase... phases) {
        this(id, EnumSet.copyOf(Arrays.asList(phases)));
    }

    /**
     * Create a trigger with a custom condition.
     *
     * @param id        Component ID
     * @param condition The condition to use
     */
    public MoonPhaseTrigger(@NotNull String id, @NotNull Condition<Player> condition) {
        this.id = id;
        this.activePhases = EnumSet.noneOf(MoonPhase.class);
        this.condition = condition;
    }

    /**
     * Create a full moon trigger.
     */
    @NotNull
    public static <I> MoonPhaseTrigger<I> fullMoon(@NotNull String id) {
        return new MoonPhaseTrigger<>(id, MoonPhase.FULL_MOON);
    }

    /**
     * Create a new moon trigger.
     */
    @NotNull
    public static <I> MoonPhaseTrigger<I> newMoon(@NotNull String id) {
        return new MoonPhaseTrigger<>(id, MoonPhase.NEW_MOON);
    }

    /**
     * Create a trigger for bright moon phases (>= 50% illumination).
     * Includes: Full Moon, Waning Gibbous, Third Quarter, First Quarter, Waxing Gibbous
     */
    @NotNull
    public static <I> MoonPhaseTrigger<I> brightMoon(@NotNull String id) {
        return new MoonPhaseTrigger<>(id, PlayerConditions.isBrightMoon());
    }

    /**
     * Create a trigger for dark moon phases (< 50% illumination).
     * Includes: Waning Crescent, New Moon, Waxing Crescent
     */
    @NotNull
    public static <I> MoonPhaseTrigger<I> darkMoon(@NotNull String id) {
        return new MoonPhaseTrigger<>(id, PlayerConditions.isDarkMoon());
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public boolean isActive(@NotNull Player player, @NotNull I instance) {
        return condition.test(player);
    }

    /**
     * Get the current moon phase for a player.
     */
    public @NotNull MoonPhase getCurrentPhase(@NotNull Player player) {
        return TimeUtil.getMoonPhaseEnum(player.getWorld());
    }

    /**
     * Get the moon brightness for a player (0.0 to 1.0).
     */
    public float getCurrentBrightness(@NotNull Player player) {
        return getCurrentPhase(player).getBrightness();
    }

    /**
     * Get the active moon phases for this trigger.
     *
     * @return Set of active phases (may be empty if using a custom condition)
     */
    public @NotNull Set<MoonPhase> getActivePhases() {
        return EnumSet.copyOf(activePhases);
    }

    /**
     * Get the condition used for moon phase checks.
     *
     * @return The condition
     */
    public @NotNull Condition<Player> getCondition() {
        return condition;
    }
}
