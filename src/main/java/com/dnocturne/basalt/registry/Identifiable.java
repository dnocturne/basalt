package com.dnocturne.basalt.registry;

/**
 * Interface for objects that can be identified by a unique string ID.
 * Used with {@link Registry} for type-safe registration and lookup.
 */
public interface Identifiable {

    /**
     * Get the unique identifier for this object.
     * IDs are typically case-insensitive when used with Registry.
     *
     * @return The unique ID
     */
    String getId();
}
