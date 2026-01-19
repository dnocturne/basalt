package com.dnocturne.basalt.component.wrapper;

import com.dnocturne.basalt.component.Component;
import com.dnocturne.basalt.component.Tickable;
import com.dnocturne.basalt.condition.Condition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * Wraps a component to only activate when a condition is true.
 *
 * <p>This wrapper tracks the condition state and properly calls the wrapped
 * component's lifecycle methods when the condition transitions:</p>
 * <ul>
 *   <li>When condition becomes true: calls wrapped.onApply()</li>
 *   <li>When condition becomes false: calls wrapped.onRemove()</li>
 *   <li>While condition is true: calls wrapped.onTick() if tickable</li>
 * </ul>
 *
 * <p>State is tracked using the provided data accessor and state key, allowing
 * the condition state to persist across ticks.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create a damage component that only activates in sunlight
 * Component<Player, AfflictionInstance> sunDamage = ConditionalComponent.builder()
 *     .id("sun_damage")
 *     .wrap(new DamageComponent<>("damage", 2.0, 20, false))
 *     .condition(PlayerConditions.isExposedToSunlight())
 *     .stateKey("sun_damage_active")
 *     .dataAccessor(AfflictionInstance::getDataMap)
 *     .tickInterval(20)
 *     .build();
 * }</pre>
 *
 * @param <E> The entity type (e.g., Player)
 * @param <I> The instance type containing state data
 */
public class ConditionalComponent<E, I> implements Tickable<E, I> {

    private final String id;
    private final Component<E, I> wrapped;
    private final Condition<E> condition;
    private final String stateKey;
    private final Function<I, Map<String, Object>> dataAccessor;
    private final int tickInterval;

    /**
     * Create a conditional component.
     *
     * @param id           Component ID
     * @param wrapped      The component to wrap
     * @param condition    The condition that must be true for the wrapped component to be active
     * @param stateKey     Key used to store the active state in the instance data
     * @param dataAccessor Function to get the data map from the instance
     * @param tickInterval How often to check the condition and tick (in tick cycles)
     */
    public ConditionalComponent(
            @NotNull String id,
            @NotNull Component<E, I> wrapped,
            @NotNull Condition<E> condition,
            @NotNull String stateKey,
            @NotNull Function<I, Map<String, Object>> dataAccessor,
            int tickInterval) {
        this.id = id;
        this.wrapped = wrapped;
        this.condition = condition;
        this.stateKey = stateKey;
        this.dataAccessor = dataAccessor;
        this.tickInterval = tickInterval > 0 ? tickInterval : 1;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public int getTickInterval() {
        return tickInterval;
    }

    @Override
    public void onApply(@NotNull E entity, @NotNull I instance) {
        // Check if condition is already true on apply
        if (condition.test(entity)) {
            wrapped.onApply(entity, instance);
            getData(instance).put(stateKey, true);
        }
    }

    @Override
    public void onTick(@NotNull E entity, @NotNull I instance) {
        boolean active = condition.test(entity);
        Map<String, Object> data = getData(instance);
        boolean wasActive = Boolean.TRUE.equals(data.get(stateKey));

        // Handle state transitions
        if (active && !wasActive) {
            // Condition just became true - apply the wrapped component
            wrapped.onApply(entity, instance);
            data.put(stateKey, true);
        } else if (!active && wasActive) {
            // Condition just became false - remove the wrapped component
            wrapped.onRemove(entity, instance);
            data.put(stateKey, false);
        }

        // Tick the wrapped component if active and tickable
        if (active && wrapped instanceof Tickable<E, I> tickable) {
            tickable.onTick(entity, instance);
        }
    }

    @Override
    public void onRemove(@NotNull E entity, @NotNull I instance) {
        Map<String, Object> data = getData(instance);
        boolean wasActive = Boolean.TRUE.equals(data.get(stateKey));

        // Clean up if the wrapped component was active
        if (wasActive) {
            wrapped.onRemove(entity, instance);
            data.put(stateKey, false);
        }
    }

    /**
     * Get the data map from the instance.
     */
    private Map<String, Object> getData(I instance) {
        return dataAccessor.apply(instance);
    }

    /**
     * Get the wrapped component.
     */
    public @NotNull Component<E, I> getWrapped() {
        return wrapped;
    }

    /**
     * Get the condition.
     */
    public @NotNull Condition<E> getCondition() {
        return condition;
    }

    /**
     * Get the state key.
     */
    public @NotNull String getStateKey() {
        return stateKey;
    }

    /**
     * Create a builder for a conditional component.
     *
     * @param <E> Entity type
     * @param <I> Instance type
     * @return A new builder
     */
    public static <E, I> Builder<E, I> builder() {
        return new Builder<>();
    }

    /**
     * Builder for conditional components.
     *
     * @param <E> Entity type
     * @param <I> Instance type
     */
    public static class Builder<E, I> {
        private String id;
        private Component<E, I> wrapped;
        private Condition<E> condition;
        private String stateKey;
        private Function<I, Map<String, Object>> dataAccessor;
        private int tickInterval = 1;

        /**
         * Set the component ID.
         */
        public Builder<E, I> id(@NotNull String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the component to wrap.
         */
        public Builder<E, I> wrap(@NotNull Component<E, I> component) {
            this.wrapped = component;
            return this;
        }

        /**
         * Set the condition.
         */
        public Builder<E, I> condition(@NotNull Condition<E> condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Set the state key for tracking active state.
         */
        public Builder<E, I> stateKey(@NotNull String stateKey) {
            this.stateKey = stateKey;
            return this;
        }

        /**
         * Set the data accessor function.
         */
        public Builder<E, I> dataAccessor(@NotNull Function<I, Map<String, Object>> dataAccessor) {
            this.dataAccessor = dataAccessor;
            return this;
        }

        /**
         * Set the tick interval.
         */
        public Builder<E, I> tickInterval(int tickInterval) {
            this.tickInterval = tickInterval;
            return this;
        }

        /**
         * Build the conditional component.
         *
         * @return The built component
         * @throws IllegalStateException if required fields are not set
         */
        public ConditionalComponent<E, I> build() {
            if (id == null) {
                throw new IllegalStateException("id is required");
            }
            if (wrapped == null) {
                throw new IllegalStateException("wrapped component is required");
            }
            if (condition == null) {
                throw new IllegalStateException("condition is required");
            }
            if (stateKey == null) {
                throw new IllegalStateException("stateKey is required");
            }
            if (dataAccessor == null) {
                throw new IllegalStateException("dataAccessor is required");
            }

            return new ConditionalComponent<>(id, wrapped, condition, stateKey, dataAccessor, tickInterval);
        }
    }
}
