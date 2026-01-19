package com.dnocturne.basalt.component;

/**
 * A component that processes logic periodically.
 *
 * <p>Tickable components are called at regular intervals determined by
 * {@link #getTickInterval()}. This is useful for periodic effects,
 * regeneration, damage over time, etc.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class RegenerationComponent implements Tickable<Player, EffectInstance> {
 *     @Override
 *     public String getId() {
 *         return "regeneration";
 *     }
 *
 *     @Override
 *     public void onTick(Player player, EffectInstance instance) {
 *         player.setHealth(Math.min(player.getHealth() + 1, player.getMaxHealth()));
 *     }
 *
 *     @Override
 *     public int getTickInterval() {
 *         return 20; // Every second
 *     }
 * }
 * }</pre>
 *
 * @param <E> The entity type this component acts on
 * @param <I> The instance type containing state data
 */
public interface Tickable<E, I> extends Component<E, I> {

    /**
     * Called every tick interval while active.
     *
     * @param entity   The entity
     * @param instance The instance containing state data
     */
    void onTick(E entity, I instance);

    /**
     * Get how often this component should tick.
     *
     * <p>The interval is in "tick cycles" of the parent system.
     * For example, if the parent ticks every 20 server ticks,
     * an interval of 2 means this component runs every 40 server ticks.</p>
     *
     * @return The tick interval (default: 1 = every tick cycle)
     */
    default int getTickInterval() {
        return 1;
    }
}
