package com.dnocturne.basalt.component;

/**
 * A component that has an activation state based on conditions.
 *
 * <p>Stateful components track whether their conditions are met and
 * provide callbacks for state transitions. This is useful for triggers,
 * conditional effects, time-of-day mechanics, etc.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class NightTrigger implements StatefulComponent<Player, EffectInstance> {
 *     @Override
 *     public String getId() {
 *         return "night_trigger";
 *     }
 *
 *     @Override
 *     public boolean isActive(Player player, EffectInstance instance) {
 *         return player.getWorld().getTime() >= 13000;
 *     }
 *
 *     @Override
 *     public void onActivate(Player player, EffectInstance instance) {
 *         player.sendMessage("Night has fallen...");
 *     }
 *
 *     @Override
 *     public void onDeactivate(Player player, EffectInstance instance) {
 *         player.sendMessage("The sun rises.");
 *     }
 * }
 * }</pre>
 *
 * @param <E> The entity type this component acts on
 * @param <I> The instance type containing state data
 */
public interface StatefulComponent<E, I> extends Component<E, I> {

    /**
     * Check if this component's conditions are currently met.
     *
     * @param entity   The entity
     * @param instance The instance containing state data
     * @return true if conditions are met and the component is active
     */
    boolean isActive(E entity, I instance);

    /**
     * Called when conditions become true (transition from inactive to active).
     *
     * @param entity   The entity
     * @param instance The instance containing state data
     */
    default void onActivate(E entity, I instance) {
    }

    /**
     * Called when conditions become false (transition from active to inactive).
     *
     * @param entity   The entity
     * @param instance The instance containing state data
     */
    default void onDeactivate(E entity, I instance) {
    }
}
