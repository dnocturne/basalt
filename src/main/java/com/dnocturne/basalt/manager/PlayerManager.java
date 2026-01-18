package com.dnocturne.basalt.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic manager for player-associated data.
 *
 * <p>This class is thread-safe and can be accessed from async operations
 * (e.g., storage callbacks, async events).</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Simple usage with factory
 * PlayerManager<MyPlayerData> manager = new PlayerManager<>(MyPlayerData::new);
 *
 * // Get or create player data
 * MyPlayerData data = manager.getOrCreate(player.getUniqueId());
 *
 * // With filtered cache
 * PlayerManager<MyPlayerData> manager = new PlayerManager<>(
 *     MyPlayerData::new,
 *     data -> data.hasSpecialStatus()
 * );
 * Collection<MyPlayerData> filtered = manager.getFiltered(); // Uses cache
 * }</pre>
 *
 * @param <T> The type of player data this manager holds
 */
public class PlayerManager<T> {

    private final Map<UUID, T> players = new ConcurrentHashMap<>();
    private final Function<UUID, T> factory;
    private final Predicate<T> filterPredicate;

    /**
     * Cached list of filtered players.
     * Invalidated when filter conditions change via {@link #invalidateFilterCache()}.
     */
    private volatile List<T> filteredCache;

    /**
     * Flag to indicate cache needs rebuilding.
     */
    private volatile boolean cacheValid = false;

    /**
     * Create a player manager with no filtered cache.
     *
     * @param factory Factory function to create new player data from UUID
     */
    public PlayerManager(Function<UUID, T> factory) {
        this(factory, null);
    }

    /**
     * Create a player manager with a filtered cache.
     *
     * @param factory         Factory function to create new player data from UUID
     * @param filterPredicate Predicate for filtering players in {@link #getFiltered()},
     *                        or null to disable filtered caching
     */
    public PlayerManager(Function<UUID, T> factory, Predicate<T> filterPredicate) {
        this.factory = factory;
        this.filterPredicate = filterPredicate;
    }

    /**
     * Get or create player data for a UUID.
     *
     * @param uuid The player UUID
     * @return The player data instance
     */
    public T getOrCreate(UUID uuid) {
        return players.computeIfAbsent(uuid, factory);
    }

    /**
     * Get player data if it exists.
     *
     * @param uuid The player UUID
     * @return Optional containing the player data, or empty if not tracked
     */
    public Optional<T> get(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    /**
     * Check if a player is being tracked.
     *
     * @param uuid The player UUID
     * @return true if tracked
     */
    public boolean isTracked(UUID uuid) {
        return players.containsKey(uuid);
    }

    /**
     * Remove a player from tracking.
     *
     * @param uuid The player UUID
     * @return The removed player data, or empty if not tracked
     */
    public Optional<T> remove(UUID uuid) {
        Optional<T> removed = Optional.ofNullable(players.remove(uuid));
        if (removed.isPresent()) {
            invalidateFilterCache();
        }
        return removed;
    }

    /**
     * Get all tracked players.
     *
     * @return Unmodifiable collection of all player data
     */
    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(players.values());
    }

    /**
     * Get all tracked players that match the filter predicate.
     * Uses cached list that is invalidated when {@link #invalidateFilterCache()} is called.
     *
     * <p>If no filter predicate was configured, this returns the same as {@link #getAll()}.</p>
     *
     * @return Collection of players matching the filter
     */
    public Collection<T> getFiltered() {
        if (filterPredicate == null) {
            return getAll();
        }

        if (cacheValid && filteredCache != null) {
            return filteredCache;
        }

        // Rebuild cache
        List<T> result = new ArrayList<>();
        for (T player : players.values()) {
            if (filterPredicate.test(player)) {
                result.add(player);
            }
        }
        filteredCache = Collections.unmodifiableList(result);
        cacheValid = true;
        return filteredCache;
    }

    /**
     * Invalidate the filtered players cache.
     * Call this when the filter condition may have changed for any player.
     */
    public void invalidateFilterCache() {
        cacheValid = false;
    }

    /**
     * Get the number of tracked players.
     *
     * @return The count of tracked players
     */
    public int size() {
        return players.size();
    }

    /**
     * Check if no players are being tracked.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Clear all player data.
     */
    public void clear() {
        players.clear();
        invalidateFilterCache();
    }
}
