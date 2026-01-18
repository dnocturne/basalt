package com.dnocturne.basalt.storage;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Generic storage abstraction for player data persistence.
 *
 * <p>Implementations handle the actual storage mechanism (JSON, SQLite, MySQL)
 * while providing a consistent async API.</p>
 *
 * @param <T> The type of player data this storage handles
 */
public interface Storage<T extends PlayerData> {

    /**
     * Initialize the storage connection.
     *
     * @return CompletableFuture that completes with true if successful
     */
    CompletableFuture<Boolean> init();

    /**
     * Shutdown the storage connection gracefully.
     *
     * @return CompletableFuture that completes when shutdown is done
     */
    CompletableFuture<Void> shutdown();

    /**
     * Load player data by UUID.
     *
     * @param uuid The player's UUID
     * @return CompletableFuture with Optional containing the data, or empty if none exists
     */
    CompletableFuture<Optional<T>> load(@NotNull UUID uuid);

    /**
     * Load player data by username.
     *
     * <p>Useful for offline-mode servers where UUIDs may change.</p>
     *
     * @param username The player's username
     * @return CompletableFuture with Optional containing the data, or empty if none exists
     */
    CompletableFuture<Optional<T>> loadByName(@NotNull String username);

    /**
     * Save player data.
     *
     * @param data The player data to save
     * @return CompletableFuture that completes when save is done
     */
    CompletableFuture<Void> save(@NotNull T data);

    /**
     * Delete player data.
     *
     * @param uuid The player's UUID
     * @return CompletableFuture that completes when deletion is done
     */
    CompletableFuture<Void> delete(@NotNull UUID uuid);

    /**
     * Check if player data exists.
     *
     * @param uuid The player's UUID
     * @return CompletableFuture with true if data exists
     */
    CompletableFuture<Boolean> exists(@NotNull UUID uuid);

    /**
     * Get the storage type name (e.g., "SQLite", "MySQL", "JSON").
     */
    @NotNull String getType();
}
