# Basalt

A shared foundation library for my Minecraft Paper plugins.

## What is this?

Basalt extracts common patterns and utilities from my plugins into a reusable base layer. This keeps individual plugins slim and focused on their domain logic rather than reimplementing generic infrastructure.

**This is not a public library.** It's designed specifically for my own plugins and may change without notice. If you're looking for a general-purpose Minecraft library, this isn't it.

## What's included

### Component System
Generic component architecture for building entity-attached behaviors:
- `Component<E, I>` - Base interface for attachable components
- `Tickable<E, I>` - Components that process on intervals
- `StatefulComponent<E, I>` - Components with active/inactive states

### Condition System
Composable predicates with logical operators:
- `Condition<T>` - Generic condition interface with `and()`, `or()`, `negate()`
- `CompositeCondition<T>` - Combines multiple conditions
- `PlayerConditions` - Pre-built conditions for Players (time, weather, moon phase, equipment, world type)

### Registry
Type-safe registration for game objects:
- `Registry<T>` - Generic registry with lookup, validation, and logging

### Player Management
- `PlayerManager<T>` - Tracks per-player state with filtering and caching
- `PlayerData` - Interface for persistable player data

### Storage
Persistence layer for player data:
- `Storage<T>` - Generic storage interface
- `AbstractSqlStorage<T>` - Base SQL implementation

### Utilities
- `TimeUtil` - Minecraft time/moon phase calculations
- `TaskUtil` - Scheduler helpers
- `MessageUtil` - Text formatting
- `ActionBarDisplay` - Action bar message management

### Listeners
- `TimeTransitionListener` - Day/night and moon phase change events

## Usage

Add as a dependency in your `build.gradle`:

```groovy
dependencies {
    implementation 'com.dnocturne:basalt:1.0.0'
}
```

## Contributing

PRs and contributions are welcome.

## License

Apache License 2.0