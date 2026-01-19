package com.dnocturne.basalt.component.trigger;

import com.dnocturne.basalt.component.StatefulComponent;
import com.dnocturne.basalt.condition.Condition;
import com.dnocturne.basalt.condition.PlayerConditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Trigger based on time of day.
 * Can trigger during day, night, or a custom time range.
 *
 * <p>Uses the Condition system for reusable time checks.</p>
 *
 * @param <I> The instance type containing state data
 */
public class TimeTrigger<I> implements StatefulComponent<Player, I> {

    private final String id;
    private final Condition<Player> condition;
    private final TimeCondition timeCondition;

    /**
     * Create a time trigger for day or night.
     *
     * @param id        The trigger ID
     * @param condition The time condition (DAY or NIGHT)
     */
    public TimeTrigger(@NotNull String id, @NotNull TimeCondition condition) {
        this.id = id;
        this.timeCondition = condition;
        this.condition = switch (condition) {
            case DAY -> PlayerConditions.isDay();
            case NIGHT -> PlayerConditions.isNight();
            case CUSTOM -> throw new IllegalArgumentException(
                    "CUSTOM requires start and end times. Use TimeTrigger(id, start, end) instead.");
        };
    }

    /**
     * Create a time trigger for a custom time range.
     *
     * @param id          The trigger ID
     * @param customStart The start time (in ticks, 0-24000)
     * @param customEnd   The end time (in ticks, 0-24000)
     */
    public TimeTrigger(@NotNull String id, long customStart, long customEnd) {
        this.id = id;
        this.timeCondition = TimeCondition.CUSTOM;
        this.condition = PlayerConditions.isTimeInRange(customStart, customEnd);
    }

    /**
     * Create a time trigger with a custom condition.
     *
     * @param id        The trigger ID
     * @param condition The custom condition
     */
    public TimeTrigger(@NotNull String id, @NotNull Condition<Player> condition) {
        this.id = id;
        this.timeCondition = TimeCondition.CUSTOM;
        this.condition = condition;
    }

    /**
     * Create a day trigger.
     */
    @NotNull
    public static <I> TimeTrigger<I> day(@NotNull String id) {
        return new TimeTrigger<>(id, TimeCondition.DAY);
    }

    /**
     * Create a night trigger.
     */
    @NotNull
    public static <I> TimeTrigger<I> night(@NotNull String id) {
        return new TimeTrigger<>(id, TimeCondition.NIGHT);
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
     * Get the time condition type for this trigger.
     *
     * @return The time condition type
     */
    public @NotNull TimeCondition getTimeCondition() {
        return timeCondition;
    }

    /**
     * Get the condition used for time checks.
     *
     * @return The condition
     */
    public @NotNull Condition<Player> getCondition() {
        return condition;
    }

    /**
     * Time condition types.
     */
    public enum TimeCondition {
        DAY,
        NIGHT,
        CUSTOM
    }
}
