package com.dnocturne.basalt.component.effect;

import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Applies a potion effect to a player.
 * Only reapplies when effect is missing or about to expire to avoid allocation overhead.
 *
 * @param <I> The instance type containing state data
 */
public class PotionEffectComponent<I> implements Tickable<Player, I> {

    /**
     * Threshold in ticks before effect expiry to trigger reapplication.
     * Using 40 ticks (2 seconds) provides buffer for tick interval variations.
     */
    private static final int REAPPLY_THRESHOLD_TICKS = 40;

    private final String id;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;
    private final int tickInterval;

    /**
     * Create a potion effect component with default settings.
     *
     * @param id         Component ID
     * @param effectType The potion effect type
     * @param amplifier  Effect amplifier (0 = level I)
     */
    public PotionEffectComponent(@NotNull String id, @NotNull PotionEffectType effectType, int amplifier) {
        this(id, effectType, amplifier, 100, true, false, true, 1);
    }

    /**
     * Create a potion effect component with full customization.
     *
     * @param id           Component ID
     * @param effectType   The potion effect type
     * @param amplifier    Effect amplifier (0 = level I)
     * @param duration     Effect duration in ticks
     * @param ambient      Whether effect is ambient (less visible particles)
     * @param particles    Whether to show particles
     * @param icon         Whether to show icon in HUD
     * @param tickInterval How often to check/reapply
     */
    public PotionEffectComponent(@NotNull String id, @NotNull PotionEffectType effectType, int amplifier,
                                  int duration, boolean ambient, boolean particles, boolean icon, int tickInterval) {
        this.id = id;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.duration = duration;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
        this.tickInterval = tickInterval;
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
        if (!needsReapplication(player)) {
            return;
        }

        player.addPotionEffect(new PotionEffect(
                effectType,
                duration,
                amplifier,
                ambient,
                particles,
                icon
        ));
    }

    /**
     * Check if the effect needs to be reapplied.
     * Returns true if effect is missing, has lower amplifier, or is about to expire.
     */
    private boolean needsReapplication(@NotNull Player player) {
        PotionEffect active = player.getPotionEffect(effectType);
        if (active == null) {
            return true;
        }
        // Reapply if current amplifier is lower than desired
        if (active.getAmplifier() < amplifier) {
            return true;
        }
        // Reapply if effect is about to expire
        return active.getDuration() <= REAPPLY_THRESHOLD_TICKS;
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull I instance) {
        player.removePotionEffect(effectType);
    }

    /**
     * Get the potion effect type.
     */
    public @NotNull PotionEffectType getEffectType() {
        return effectType;
    }

    /**
     * Get the effect amplifier.
     */
    public int getAmplifier() {
        return amplifier;
    }

    /**
     * Get the effect duration in ticks.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Check if the effect is ambient.
     */
    public boolean isAmbient() {
        return ambient;
    }

    /**
     * Check if particles are shown.
     */
    public boolean hasParticles() {
        return particles;
    }

    /**
     * Check if the icon is shown.
     */
    public boolean hasIcon() {
        return icon;
    }
}
