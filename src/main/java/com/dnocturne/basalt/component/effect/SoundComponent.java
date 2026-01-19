package com.dnocturne.basalt.component.effect;

import com.dnocturne.basalt.component.Tickable;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Plays sounds to the player periodically or on apply/remove.
 *
 * @param <I> The instance type containing state data
 */
public class SoundComponent<I> implements Tickable<Player, I> {

    private final String id;
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final int tickInterval;
    private final boolean playOnApply;
    private final boolean playOnRemove;

    /**
     * Create a sound component that plays periodically.
     *
     * @param id           Component ID
     * @param sound        The sound to play
     * @param volume       Sound volume (1.0 = normal)
     * @param pitch        Sound pitch (1.0 = normal)
     * @param tickInterval How often to play (0 = never tick, use onApply/onRemove)
     */
    public SoundComponent(@NotNull String id, @NotNull Sound sound, float volume, float pitch, int tickInterval) {
        this(id, sound, volume, pitch, tickInterval, false, false);
    }

    /**
     * Create a sound component with full customization.
     *
     * @param id           Component ID
     * @param sound        The sound to play
     * @param volume       Sound volume (1.0 = normal)
     * @param pitch        Sound pitch (1.0 = normal)
     * @param tickInterval How often to play (0 = never tick, use onApply/onRemove)
     * @param playOnApply  Whether to play when component is applied
     * @param playOnRemove Whether to play when component is removed
     */
    public SoundComponent(@NotNull String id, @NotNull Sound sound, float volume, float pitch,
                           int tickInterval, boolean playOnApply, boolean playOnRemove) {
        this.id = id;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.tickInterval = tickInterval;
        this.playOnApply = playOnApply;
        this.playOnRemove = playOnRemove;
    }

    /**
     * Create a sound component that only plays on apply.
     */
    public static <I> SoundComponent<I> onApply(@NotNull String id, @NotNull Sound sound, float volume, float pitch) {
        return new SoundComponent<>(id, sound, volume, pitch, 0, true, false);
    }

    /**
     * Create a sound component that plays on apply and remove.
     */
    public static <I> SoundComponent<I> onApplyAndRemove(@NotNull String id, @NotNull Sound sound, float volume, float pitch) {
        return new SoundComponent<>(id, sound, volume, pitch, 0, true, true);
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public int getTickInterval() {
        // If tickInterval is 0, we don't want to tick but might still be used
        // Return 1 as a safe default, the component just won't do anything on tick
        return tickInterval > 0 ? tickInterval : 1;
    }

    @Override
    public void onApply(@NotNull Player player, @NotNull I instance) {
        if (playOnApply) {
            playSound(player);
        }
    }

    @Override
    public void onTick(@NotNull Player player, @NotNull I instance) {
        if (tickInterval > 0) {
            playSound(player);
        }
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull I instance) {
        if (playOnRemove) {
            playSound(player);
        }
    }

    private void playSound(@NotNull Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Get the sound.
     */
    public @NotNull Sound getSound() {
        return sound;
    }

    /**
     * Get the volume.
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Get the pitch.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Check if sound plays on apply.
     */
    public boolean isPlayOnApply() {
        return playOnApply;
    }

    /**
     * Check if sound plays on remove.
     */
    public boolean isPlayOnRemove() {
        return playOnRemove;
    }
}
