package com.dnocturne.basalt.condition;

/**
 * A condition that can be evaluated against a context of type T.
 *
 * <p>Conditions are reusable, composable predicates that can be combined
 * using logical operators ({@link #and}, {@link #or}, {@link #negate}).</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Condition<Player> canAct = isAlive()
 *     .and(isNotFrozen())
 *     .and(hasPermission("action"));
 *
 * if (canAct.test(player)) {
 *     // Player can perform the action
 * }
 * }</pre>
 *
 * @param <T> The type of context this condition tests against
 * @see CompositeCondition
 */
@FunctionalInterface
public interface Condition<T> {

    /**
     * Test if this condition is met for the given context.
     *
     * @param context The context to test
     * @return true if the condition is met
     */
    boolean test(T context);

    /**
     * Get a human-readable description of this condition.
     *
     * @return The condition description
     */
    default String describe() {
        return getClass().getSimpleName();
    }

    /**
     * Combine this condition with another using logical AND.
     *
     * @param other The other condition
     * @return A new condition that is true only if both conditions are true
     */
    default Condition<T> and(Condition<T> other) {
        return CompositeCondition.and(this, other);
    }

    /**
     * Combine this condition with another using logical OR.
     *
     * @param other The other condition
     * @return A new condition that is true if either condition is true
     */
    default Condition<T> or(Condition<T> other) {
        return CompositeCondition.or(this, other);
    }

    /**
     * Negate this condition.
     *
     * @return A new condition that is true only if this condition is false
     */
    default Condition<T> negate() {
        return CompositeCondition.not(this);
    }

    /**
     * Create a condition that always returns true.
     *
     * @param <T> The context type
     * @return A condition that always passes
     */
    static <T> Condition<T> always() {
        return context -> true;
    }

    /**
     * Create a condition that always returns false.
     *
     * @param <T> The context type
     * @return A condition that always fails
     */
    static <T> Condition<T> never() {
        return context -> false;
    }

    /**
     * Create a condition with a custom description.
     *
     * @param description The description
     * @param condition   The condition logic
     * @param <T>         The context type
     * @return A described condition
     */
    static <T> Condition<T> of(String description, Condition<T> condition) {
        return new Condition<>() {
            @Override
            public boolean test(T context) {
                return condition.test(context);
            }

            @Override
            public String describe() {
                return description;
            }
        };
    }
}
