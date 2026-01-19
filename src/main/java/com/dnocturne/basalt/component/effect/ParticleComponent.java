package com.dnocturne.basalt.component.effect;

import com.dnocturne.basalt.component.Tickable;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Spawns particles around the player periodically.
 *
 * @param <I> The instance type containing state data
 */
public class ParticleComponent<I> implements Tickable<Player, I> {

    private final String id;
    private final Particle type;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final int tickInterval;
    private final double heightOffset;

    /**
     * Create a particle component with default settings.
     *
     * @param id           Component ID
     * @param type         Particle type
     * @param count        Number of particles
     * @param tickInterval How often to spawn particles
     */
    public ParticleComponent(@NotNull String id, @NotNull Particle type, int count, int tickInterval) {
        this(id, type, count, 0.3, 0.5, 0.3, 0.01, tickInterval, 1.0);
    }

    /**
     * Create a particle component with full customization.
     *
     * @param id           Component ID
     * @param type         Particle type
     * @param count        Number of particles
     * @param offsetX      Random offset on X axis
     * @param offsetY      Random offset on Y axis
     * @param offsetZ      Random offset on Z axis
     * @param speed        Particle speed
     * @param tickInterval How often to spawn particles
     * @param heightOffset Height offset from player's feet
     */
    public ParticleComponent(@NotNull String id, @NotNull Particle type, int count,
                              double offsetX, double offsetY, double offsetZ, double speed,
                              int tickInterval, double heightOffset) {
        this.id = id;
        this.type = type;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.tickInterval = tickInterval;
        this.heightOffset = heightOffset;
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
        player.getWorld().spawnParticle(
                type,
                player.getLocation().add(0, heightOffset, 0),
                count,
                offsetX, offsetY, offsetZ,
                speed
        );
    }

    /**
     * Get the particle type.
     */
    public @NotNull Particle getType() {
        return type;
    }

    /**
     * Get the particle count.
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the X offset.
     */
    public double getOffsetX() {
        return offsetX;
    }

    /**
     * Get the Y offset.
     */
    public double getOffsetY() {
        return offsetY;
    }

    /**
     * Get the Z offset.
     */
    public double getOffsetZ() {
        return offsetZ;
    }

    /**
     * Get the particle speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Get the height offset.
     */
    public double getHeightOffset() {
        return heightOffset;
    }
}
