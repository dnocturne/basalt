package com.dnocturne.basalt.component;

/**
 * Base interface for reusable components with lifecycle hooks.
 *
 * <p>Components are building blocks that define behavior for entities.
 * They provide lifecycle methods called when the component is applied
 * or removed from an entity.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class DamageComponent implements Component<Player, EffectInstance> {
 *     @Override
 *     public String getId() {
 *         return "damage";
 *     }
 *
 *     @Override
 *     public void onApply(Player player, EffectInstance instance) {
 *         // Apply initial damage or setup
 *     }
 *
 *     @Override
 *     public void onRemove(Player player, EffectInstance instance) {
 *         // Cleanup when removed
 *     }
 * }
 * }</pre>
 *
 * @param <E> The entity type this component acts on (e.g., Player, Entity)
 * @param <I> The instance type containing state data
 */
public interface Component<E, I> {

    /**
     * Get the unique identifier for this component type.
     *
     * @return The component ID
     */
    String getId();

    /**
     * Called when this component is first applied to an entity.
     *
     * @param entity   The entity
     * @param instance The instance containing state data
     */
    default void onApply(E entity, I instance) {
    }

    /**
     * Called when this component is removed from an entity.
     *
     * @param entity   The entity
     * @param instance The instance containing state data
     */
    default void onRemove(E entity, I instance) {
    }
}
