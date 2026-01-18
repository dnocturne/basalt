package com.dnocturne.basalt.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Utilities for sending messages using Adventure/MiniMessage.
 */
public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // Use hex character (&#RRGGBB format) for PlaceholderAPI compatibility
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MessageUtil() {
    }

    /**
     * Get the shared MiniMessage instance.
     */
    public static @NotNull MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }

    /**
     * Parse a MiniMessage string into a Component.
     */
    public static @NotNull Component parse(@NotNull String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Parse a MiniMessage string with placeholders.
     */
    public static @NotNull Component parse(@NotNull String message, @NotNull TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(message, resolvers);
    }

    /**
     * Parse a MiniMessage string with simple key-value placeholders.
     */
    public static @NotNull Component parse(@NotNull String message, @NotNull Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((key, value) ->
                builder.resolver(Placeholder.parsed(key, value))
        );
        return MINI_MESSAGE.deserialize(message, builder.build());
    }

    /**
     * Send a MiniMessage to a player.
     */
    public static void send(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(parse(message));
    }

    /**
     * Send a MiniMessage with placeholders to a player.
     */
    public static void send(@NotNull CommandSender sender, @NotNull String message, @NotNull TagResolver... resolvers) {
        sender.sendMessage(parse(message, resolvers));
    }

    /**
     * Send a MiniMessage with key-value placeholders.
     */
    public static void send(@NotNull CommandSender sender, @NotNull String message, @NotNull Map<String, String> placeholders) {
        sender.sendMessage(parse(message, placeholders));
    }

    /**
     * Send an action bar message to a player.
     */
    public static void sendActionBar(@NotNull Player player, @NotNull String message) {
        player.sendActionBar(parse(message));
    }

    /**
     * Send an action bar message with placeholders.
     */
    public static void sendActionBar(@NotNull Player player, @NotNull String message, @NotNull TagResolver... resolvers) {
        player.sendActionBar(parse(message, resolvers));
    }

    /**
     * Create a simple placeholder resolver.
     */
    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull String value) {
        return Placeholder.parsed(key, value);
    }

    /**
     * Create a component placeholder resolver.
     */
    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull Component value) {
        return Placeholder.component(key, value);
    }

    /**
     * Convert a MiniMessage string to legacy color codes.
     * Useful for PlaceholderAPI which doesn't support MiniMessage natively.
     *
     * @param miniMessage The MiniMessage formatted string
     * @return Legacy formatted string with color codes, or null/empty if input is null/empty
     */
    public static @Nullable String toLegacy(@Nullable String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return miniMessage;
        }
        Component component = MINI_MESSAGE.deserialize(miniMessage);
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Convert a Component to legacy color codes.
     *
     * @param component The component to convert
     * @return Legacy formatted string with color codes, empty string if component is null
     */
    public static @NotNull String toLegacy(@Nullable Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(component);
    }
}
