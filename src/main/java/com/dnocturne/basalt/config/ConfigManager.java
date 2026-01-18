package com.dnocturne.basalt.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages configuration files using BoostedYAML.
 *
 * <p>Provides automatic loading, reloading, and saving of YAML configuration files
 * with support for defaults from plugin resources and automatic updates.</p>
 */
public class ConfigManager {

    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, YamlDocument> configs = new HashMap<>();

    private @Nullable YamlDocument mainConfig;

    /**
     * Create a new ConfigManager.
     *
     * @param plugin The plugin instance
     */
    public ConfigManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Load the main config.yml file.
     *
     * @return true if successful
     */
    public boolean load() {
        try {
            mainConfig = loadConfig("config.yml");
            configs.put("config", mainConfig);
            logger.info("Configuration loaded successfully");
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load configuration", e);
            return false;
        }
    }

    /**
     * Reload all loaded configuration files.
     */
    public void reload() {
        try {
            for (YamlDocument doc : configs.values()) {
                doc.reload();
            }
            logger.info("Configuration reloaded successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to reload configuration", e);
        }
    }

    /**
     * Save all configuration files.
     */
    public void save() {
        try {
            for (YamlDocument doc : configs.values()) {
                doc.save();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save configuration", e);
        }
    }

    /**
     * Load a configuration file with defaults from resources.
     *
     * <p>The config will auto-update to include new keys from defaults while
     * preserving user-modified values and comments.</p>
     *
     * @param fileName The config file name (e.g., "config.yml")
     * @return The loaded YamlDocument
     * @throws IOException If loading fails
     */
    public @NotNull YamlDocument loadConfig(@NotNull String fileName) throws IOException {
        InputStream resource = plugin.getResource(fileName);
        Objects.requireNonNull(resource, "Resource not found: " + fileName);

        return YamlDocument.create(
                new File(plugin.getDataFolder(), fileName),
                resource,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                        .setVersioning(new BasicVersioning("config-version"))
                        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                        .build()
        );
    }

    /**
     * Load a configuration file with defaults, using a custom version key.
     *
     * @param fileName   The config file name
     * @param versionKey The key used for versioning (e.g., "config-version")
     * @return The loaded YamlDocument
     * @throws IOException If loading fails
     */
    public @NotNull YamlDocument loadConfig(@NotNull String fileName, @NotNull String versionKey) throws IOException {
        InputStream resource = plugin.getResource(fileName);
        Objects.requireNonNull(resource, "Resource not found: " + fileName);

        return YamlDocument.create(
                new File(plugin.getDataFolder(), fileName),
                resource,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                        .setVersioning(new BasicVersioning(versionKey))
                        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                        .build()
        );
    }

    /**
     * Load a configuration file without defaults (pure data file).
     *
     * <p>Use this for data files that shouldn't have defaults from resources.</p>
     *
     * @param fileName The data file name
     * @return The loaded YamlDocument
     * @throws IOException If loading fails
     */
    public @NotNull YamlDocument loadDataFile(@NotNull String fileName) throws IOException {
        return YamlDocument.create(
                new File(plugin.getDataFolder(), fileName),
                GeneralSettings.DEFAULT,
                LoaderSettings.DEFAULT,
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
        );
    }

    /**
     * Load a config and register it by name.
     *
     * @param name     The name to register the config under
     * @param fileName The config file name
     * @return The loaded YamlDocument
     * @throws IOException If loading fails
     */
    public @NotNull YamlDocument loadAndRegister(@NotNull String name, @NotNull String fileName) throws IOException {
        YamlDocument doc = loadConfig(fileName);
        configs.put(name, doc);
        return doc;
    }

    /**
     * Register an already-loaded config.
     *
     * @param name The name to register under
     * @param doc  The YamlDocument to register
     */
    public void register(@NotNull String name, @NotNull YamlDocument doc) {
        configs.put(name, doc);
    }

    /**
     * Get the main configuration (config.yml).
     *
     * @return The main config, or null if not loaded
     */
    public @Nullable YamlDocument getMainConfig() {
        return mainConfig;
    }

    /**
     * Get a configuration by registered name.
     *
     * @param name The registered name
     * @return The config, or null if not found
     */
    public @Nullable YamlDocument getConfig(@NotNull String name) {
        return configs.get(name);
    }

    /**
     * Check if a config is registered.
     *
     * @param name The name to check
     * @return true if registered
     */
    public boolean hasConfig(@NotNull String name) {
        return configs.containsKey(name);
    }

    /**
     * Load a config from a subdirectory and register it.
     *
     * <p>Useful for plugins with category-based configs (e.g., afflictions/vampirism.yml).</p>
     *
     * @param subdirectory The subdirectory name (e.g., "afflictions")
     * @param configId     The config identifier (e.g., "vampirism")
     * @return The loaded YamlDocument
     * @throws IOException If loading fails
     */
    public @NotNull YamlDocument loadSubdirectoryConfig(@NotNull String subdirectory, @NotNull String configId) throws IOException {
        String fileName = subdirectory + "/" + configId + ".yml";
        YamlDocument doc = loadConfig(fileName);
        configs.put(subdirectory + ":" + configId, doc);
        return doc;
    }

    /**
     * Get the plugin instance.
     */
    public @NotNull Plugin getPlugin() {
        return plugin;
    }
}
