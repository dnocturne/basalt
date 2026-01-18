package com.dnocturne.basalt.storage;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Base interface for player data that can be stored.
 *
 * <p>Implement this interface in your plugin to define what data
 * should be persisted for each player.</p>
 */
public interface PlayerData {

    /**
     * Get the player's UUID.
     */
    @NotNull UUID getUuid();

    /**
     * Get the player's username.
     */
    @NotNull String getUsername();
}
