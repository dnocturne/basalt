package com.dnocturne.basalt.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * A generic registry for registering and retrieving objects by string ID.
 * <p>
 * Features:
 * <ul>
 *   <li>Case-insensitive ID lookup</li>
 *   <li>Conflict detection with detailed error messages</li>
 *   <li>Optional logging of registration events</li>
 *   <li>Type-safe generics</li>
 * </ul>
 * <p>
 * Example usage with {@link Identifiable}:
 * <pre>{@code
 * Registry<MyType> registry = Registry.forIdentifiable();
 * registry.register(myObject);
 * Optional<MyType> result = registry.get("my-id");
 * }</pre>
 * <p>
 * Example usage with custom ID extractor:
 * <pre>{@code
 * Registry<MyType> registry = new Registry<>(MyType::getName);
 * registry.register(myObject);
 * }</pre>
 *
 * @param <T> The type of objects stored in this registry
 */
public class Registry<T> {

    private final Map<String, T> entries = new HashMap<>();
    private final Function<T, String> idExtractor;
    private Function<T, String> displayNameExtractor;
    private final String typeName;
    private Logger logger;

    /**
     * Create a registry for {@link Identifiable} objects.
     *
     * @param <T> The type extending Identifiable
     * @return A new registry using {@link Identifiable#getId()} to extract IDs
     */
    public static <T extends Identifiable> Registry<T> forIdentifiable() {
        return new Registry<>(Identifiable::getId, "entry");
    }

    /**
     * Create a registry for {@link Identifiable} objects with a custom type name.
     *
     * @param typeName The name of the type for log messages (e.g., "affliction", "ability")
     * @param <T>      The type extending Identifiable
     * @return A new registry using {@link Identifiable#getId()} to extract IDs
     */
    public static <T extends Identifiable> Registry<T> forIdentifiable(String typeName) {
        return new Registry<>(Identifiable::getId, typeName);
    }

    /**
     * Create a registry with a custom ID extractor function.
     *
     * @param idExtractor Function to extract the ID from an object
     */
    public Registry(Function<T, String> idExtractor) {
        this(idExtractor, "entry");
    }

    /**
     * Create a registry with a custom ID extractor and type name.
     *
     * @param idExtractor Function to extract the ID from an object
     * @param typeName    The name of the type for log messages
     */
    public Registry(Function<T, String> idExtractor, String typeName) {
        this.idExtractor = idExtractor;
        this.typeName = typeName;
    }

    /**
     * Set the logger for diagnostic messages.
     * If not set, registration conflicts will only throw exceptions without logging.
     *
     * @param logger The logger to use
     * @return This registry for chaining
     */
    public Registry<T> setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Set a custom display name extractor for error messages and logging.
     * If not set, falls back to the ID extractor.
     *
     * @param displayNameExtractor Function to extract display name from an object
     * @return This registry for chaining
     */
    public Registry<T> setDisplayNameExtractor(Function<T, String> displayNameExtractor) {
        this.displayNameExtractor = displayNameExtractor;
        return this;
    }

    /**
     * Register an object.
     *
     * @param entry The object to register
     * @throws IllegalArgumentException if an object with the same ID already exists
     */
    public void register(T entry) {
        String id = idExtractor.apply(entry).toLowerCase();
        if (entries.containsKey(id)) {
            T existing = entries.get(id);
            String errorMsg = String.format(
                    "%s ID conflict: Cannot register '%s' (class: %s) - " +
                    "ID '%s' is already registered by '%s' (class: %s). " +
                    "Note: IDs are case-insensitive.",
                    capitalize(typeName),
                    getDisplayName(entry),
                    entry.getClass().getName(),
                    id,
                    getDisplayName(existing),
                    existing.getClass().getName()
            );
            if (logger != null) {
                logger.severe(errorMsg);
            }
            throw new IllegalArgumentException(errorMsg);
        }
        entries.put(id, entry);
        if (logger != null) {
            logger.info("Registered " + typeName + ": " + id);
        }
    }

    /**
     * Register an object, replacing any existing entry with the same ID.
     *
     * @param entry The object to register
     * @return The previously registered object, or null if none
     */
    public T registerOrReplace(T entry) {
        String id = idExtractor.apply(entry).toLowerCase();
        T previous = entries.put(id, entry);
        if (logger != null) {
            if (previous != null) {
                logger.info("Replaced " + typeName + ": " + id);
            } else {
                logger.info("Registered " + typeName + ": " + id);
            }
        }
        return previous;
    }

    /**
     * Unregister an object by ID.
     *
     * @param id The ID to unregister
     * @return true if the object was unregistered
     */
    public boolean unregister(String id) {
        T removed = entries.remove(id.toLowerCase());
        if (removed != null && logger != null) {
            logger.info("Unregistered " + typeName + ": " + id.toLowerCase());
        }
        return removed != null;
    }

    /**
     * Get an object by ID.
     *
     * @param id The object ID
     * @return Optional containing the object, or empty if not found
     */
    public Optional<T> get(String id) {
        return Optional.ofNullable(entries.get(id.toLowerCase()));
    }

    /**
     * Check if an object is registered.
     *
     * @param id The object ID
     * @return true if registered
     */
    public boolean isRegistered(String id) {
        return entries.containsKey(id.toLowerCase());
    }

    /**
     * Get all registered objects.
     *
     * @return Unmodifiable collection of all objects
     */
    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(entries.values());
    }

    /**
     * Get all registered IDs.
     *
     * @return Unmodifiable collection of all IDs (lowercase)
     */
    public Collection<String> getAllIds() {
        return Collections.unmodifiableCollection(entries.keySet());
    }

    /**
     * Get the number of registered entries.
     *
     * @return The count of registered objects
     */
    public int size() {
        return entries.size();
    }

    /**
     * Check if the registry is empty.
     *
     * @return true if no objects are registered
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Clear all registered objects.
     */
    public void clear() {
        entries.clear();
        if (logger != null) {
            logger.info("Cleared all " + typeName + " entries");
        }
    }

    private String getDisplayName(T entry) {
        if (displayNameExtractor != null) {
            return displayNameExtractor.apply(entry);
        }
        return idExtractor.apply(entry);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
