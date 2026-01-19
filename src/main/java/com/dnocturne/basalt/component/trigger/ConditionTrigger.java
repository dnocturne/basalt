package com.dnocturne.basalt.component.trigger;

import com.dnocturne.basalt.component.StatefulComponent;
import com.dnocturne.basalt.condition.Condition;
import com.dnocturne.basalt.condition.PlayerConditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Generic trigger based on any Condition.
 * Provides factory methods for common environmental triggers.
 *
 * @param <I> The instance type containing state data
 */
public class ConditionTrigger<I> implements StatefulComponent<Player, I> {

    private final String id;
    private final Condition<Player> condition;

    /**
     * Create a condition trigger.
     *
     * @param id        Component ID
     * @param condition The condition to use
     */
    public ConditionTrigger(@NotNull String id, @NotNull Condition<Player> condition) {
        this.id = id;
        this.condition = condition;
    }

    // ===== Factory Methods =====

    /**
     * Create a trigger that activates when the player is exposed to sunlight.
     * (Daytime + sky access + clear weather)
     */
    @NotNull
    public static <I> ConditionTrigger<I> sunlight(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isExposedToSunlight());
    }

    /**
     * Create a trigger that activates when the player is protected from sunlight.
     */
    @NotNull
    public static <I> ConditionTrigger<I> protectedFromSunlight(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isProtectedFromSunlight());
    }

    /**
     * Create a trigger that activates when the player is underground (no sky access).
     */
    @NotNull
    public static <I> ConditionTrigger<I> underground(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isUnderCover());
    }

    /**
     * Create a trigger that activates when the player has sky access.
     */
    @NotNull
    public static <I> ConditionTrigger<I> skyAccess(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.hasSkyAccess());
    }

    /**
     * Create a trigger that activates during rain or thunderstorm.
     */
    @NotNull
    public static <I> ConditionTrigger<I> storm(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.hasStorm());
    }

    /**
     * Create a trigger that activates during clear weather.
     */
    @NotNull
    public static <I> ConditionTrigger<I> clearWeather(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isClearWeather());
    }

    /**
     * Create a trigger that activates during thunderstorm.
     */
    @NotNull
    public static <I> ConditionTrigger<I> thunderstorm(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isThundering());
    }

    /**
     * Create a trigger that activates when the player is in the Overworld.
     */
    @NotNull
    public static <I> ConditionTrigger<I> overworld(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isInOverworld());
    }

    /**
     * Create a trigger that activates when the player is in the Nether.
     */
    @NotNull
    public static <I> ConditionTrigger<I> nether(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isInNether());
    }

    /**
     * Create a trigger that activates when the player is in the End.
     */
    @NotNull
    public static <I> ConditionTrigger<I> end(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isInEnd());
    }

    /**
     * Create a trigger that activates when the player is wearing a helmet.
     */
    @NotNull
    public static <I> ConditionTrigger<I> hasHelmet(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.hasHelmet());
    }

    /**
     * Create a trigger that activates when the player is not wearing a helmet.
     */
    @NotNull
    public static <I> ConditionTrigger<I> noHelmet(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.noHelmet());
    }

    /**
     * Create a trigger that activates during a full moon night.
     */
    @NotNull
    public static <I> ConditionTrigger<I> fullMoonNight(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isFullMoonNight());
    }

    /**
     * Create a trigger that activates during a bright moon night (>= 50% illumination).
     */
    @NotNull
    public static <I> ConditionTrigger<I> brightMoonNight(@NotNull String id) {
        return new ConditionTrigger<>(id, PlayerConditions.isBrightMoonNight());
    }

    /**
     * Create a trigger that is always active.
     */
    @NotNull
    public static <I> ConditionTrigger<I> always(@NotNull String id) {
        return new ConditionTrigger<>(id, player -> true);
    }

    /**
     * Create a trigger that is never active.
     */
    @NotNull
    public static <I> ConditionTrigger<I> never(@NotNull String id) {
        return new ConditionTrigger<>(id, player -> false);
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
     * Get the condition used by this trigger.
     *
     * @return The condition
     */
    public @NotNull Condition<Player> getCondition() {
        return condition;
    }

    /**
     * Create a new trigger with the condition negated.
     */
    public @NotNull ConditionTrigger<I> negate() {
        return new ConditionTrigger<>(id + "_negated", condition.negate());
    }

    /**
     * Create a new trigger that requires both this and another condition.
     */
    public @NotNull ConditionTrigger<I> and(@NotNull Condition<Player> other) {
        return new ConditionTrigger<>(id + "_and", condition.and(other));
    }

    /**
     * Create a new trigger that requires either this or another condition.
     */
    public @NotNull ConditionTrigger<I> or(@NotNull Condition<Player> other) {
        return new ConditionTrigger<>(id + "_or", condition.or(other));
    }
}
