package com.dnocturne.basalt.component.effect;

import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Deals damage to the player over time.
 * Configurable damage amount, interval, and armor bypass.
 *
 * @param <I> The instance type containing state data
 */
public class DamageComponent<I> implements Tickable<Player, I> {

    private final String id;
    private final double damage;
    private final int tickInterval;
    private final boolean bypassArmor;

    /**
     * Create a damage component with default settings (respects armor).
     *
     * @param id           Component ID
     * @param damage       Damage per tick
     * @param tickInterval How often to deal damage
     */
    public DamageComponent(@NotNull String id, double damage, int tickInterval) {
        this(id, damage, tickInterval, false);
    }

    /**
     * Create a damage component with full customization.
     *
     * @param id           Component ID
     * @param damage       Damage per tick
     * @param tickInterval How often to deal damage
     * @param bypassArmor  Whether to bypass armor (direct health reduction)
     */
    public DamageComponent(@NotNull String id, double damage, int tickInterval, boolean bypassArmor) {
        this.id = id;
        this.damage = damage;
        this.tickInterval = tickInterval;
        this.bypassArmor = bypassArmor;
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
    public void onTick(@NotNull Player player, @NotNull I instance) {
        if (bypassArmor) {
            // Direct health reduction - bypasses armor
            player.setHealth(Math.max(0, player.getHealth() - damage));
        } else {
            // Normal damage - respects armor
            player.damage(damage);
        }
    }

    /**
     * Get the damage dealt per tick.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Check if damage bypasses armor.
     */
    public boolean isBypassArmor() {
        return bypassArmor;
    }
}
