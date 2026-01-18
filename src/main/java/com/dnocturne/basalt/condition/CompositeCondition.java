package com.dnocturne.basalt.condition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A condition composed of multiple sub-conditions with logical operators.
 *
 * <p>Supports AND, OR, and NOT operations for building complex conditions.</p>
 *
 * @param <T> The type of context this condition tests against
 */
public final class CompositeCondition<T> implements Condition<T> {

    private final List<Condition<T>> conditions;
    private final Operator operator;

    private CompositeCondition(Operator operator, List<Condition<T>> conditions) {
        this.operator = operator;
        this.conditions = List.copyOf(conditions);
    }

    /**
     * Create a condition that is true only if ALL sub-conditions are true.
     *
     * @param conditions The conditions to combine
     * @param <T>        The context type
     * @return A composite AND condition
     */
    @SafeVarargs
    public static <T> Condition<T> and(Condition<T>... conditions) {
        if (conditions.length == 0) {
            return Condition.always();
        }
        if (conditions.length == 1) {
            return conditions[0];
        }
        return new CompositeCondition<>(Operator.AND, Arrays.asList(conditions));
    }

    /**
     * Create a condition that is true if ANY sub-condition is true.
     *
     * @param conditions The conditions to combine
     * @param <T>        The context type
     * @return A composite OR condition
     */
    @SafeVarargs
    public static <T> Condition<T> or(Condition<T>... conditions) {
        if (conditions.length == 0) {
            return Condition.never();
        }
        if (conditions.length == 1) {
            return conditions[0];
        }
        return new CompositeCondition<>(Operator.OR, Arrays.asList(conditions));
    }

    /**
     * Create a condition that negates the given condition.
     *
     * @param condition The condition to negate
     * @param <T>       The context type
     * @return A negated condition
     */
    public static <T> Condition<T> not(Condition<T> condition) {
        // Double negation optimization
        if (condition instanceof CompositeCondition<T> composite && composite.operator == Operator.NOT) {
            return composite.conditions.get(0);
        }
        return new CompositeCondition<>(Operator.NOT, List.of(condition));
    }

    @Override
    public boolean test(T context) {
        return switch (operator) {
            case AND -> testAnd(context);
            case OR -> testOr(context);
            case NOT -> !conditions.get(0).test(context);
        };
    }

    private boolean testAnd(T context) {
        for (Condition<T> condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    private boolean testOr(T context) {
        for (Condition<T> condition : conditions) {
            if (condition.test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String describe() {
        return switch (operator) {
            case AND -> "(" + conditions.stream()
                    .map(Condition::describe)
                    .collect(Collectors.joining(" AND ")) + ")";
            case OR -> "(" + conditions.stream()
                    .map(Condition::describe)
                    .collect(Collectors.joining(" OR ")) + ")";
            case NOT -> "NOT " + conditions.get(0).describe();
        };
    }

    private enum Operator {
        AND,
        OR,
        NOT
    }
}
