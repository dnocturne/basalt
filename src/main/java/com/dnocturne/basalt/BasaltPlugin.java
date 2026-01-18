package com.dnocturne.basalt;

import com.dnocturne.basalt.config.ConfigManager;
import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.basalt.util.TaskUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for plugins using the Basalt framework.
 *
 * <p>Provides automatic initialization of common plugin infrastructure:</p>
 * <ul>
 *   <li>{@link ConfigManager} - Configuration file management</li>
 *   <li>{@link LocalizationManager} - Multi-language support</li>
 *   <li>{@link TaskUtil} - Task scheduling utilities</li>
 * </ul>
 *
 * <p>Subclasses should override {@link #enable()} and {@link #disable()} instead
 * of {@link #onEnable()} and {@link #onDisable()}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyPlugin extends BasaltPlugin {
 *     @Override
 *     protected void enable() {
 *         // Plugin-specific initialization
 *         getLocalizationManager()
 *             .availableLanguages("en", "es", "de")
 *             .defaultLanguage("en")
 *             .languageFromConfig(() -> getConfigManager().getMainConfig().getString("language", "en"))
 *             .load();
 *     }
 *
 *     @Override
 *     protected void disable() {
 *         // Plugin-specific cleanup
 *     }
 * }
 * }</pre>
 */
public abstract class BasaltPlugin extends JavaPlugin {

    private @Nullable ConfigManager configManager;
    private @Nullable LocalizationManager localizationManager;

    /**
     * Internal enable method. Do not override - use {@link #enable()} instead.
     */
    @Override
    public final void onEnable() {
        // Initialize TaskUtil with this plugin
        TaskUtil.init(this);

        // Create managers (lazy initialization - plugins call load() when ready)
        configManager = new ConfigManager(this);
        localizationManager = new LocalizationManager(this);

        // Load main config by default
        if (getResource("config.yml") != null) {
            configManager.load();
        }

        // Call plugin-specific enable logic
        enable();
    }

    /**
     * Internal disable method. Do not override - use {@link #disable()} instead.
     */
    @Override
    public final void onDisable() {
        // Call plugin-specific disable logic first
        disable();

        // Save configs
        if (configManager != null) {
            configManager.save();
        }

        // Clean up TaskUtil
        TaskUtil.shutdown();

        // Clear references
        configManager = null;
        localizationManager = null;
    }

    /**
     * Called when the plugin is enabled.
     *
     * <p>Override this method to add your plugin's initialization logic.
     * The {@link ConfigManager} and {@link LocalizationManager} are already
     * initialized when this method is called.</p>
     *
     * <p>If you have a config.yml resource, it will be automatically loaded.
     * For localization, call {@link LocalizationManager#load()} after configuring it.</p>
     */
    protected abstract void enable();

    /**
     * Called when the plugin is disabled.
     *
     * <p>Override this method to add your plugin's cleanup logic.
     * Configs will be saved automatically after this method completes.</p>
     */
    protected abstract void disable();

    /**
     * Get the configuration manager.
     *
     * @return The config manager
     * @throws IllegalStateException If called before plugin is enabled
     */
    public @NotNull ConfigManager getConfigManager() {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager not initialized - plugin not enabled yet");
        }
        return configManager;
    }

    /**
     * Get the localization manager.
     *
     * @return The localization manager
     * @throws IllegalStateException If called before plugin is enabled
     */
    public @NotNull LocalizationManager getLocalizationManager() {
        if (localizationManager == null) {
            throw new IllegalStateException("LocalizationManager not initialized - plugin not enabled yet");
        }
        return localizationManager;
    }

    /**
     * Reload the plugin's configuration and localization.
     *
     * <p>Override this method to add additional reload logic. Be sure to call
     * {@code super.reload()} to reload configs and localization.</p>
     */
    public void reload() {
        if (configManager != null) {
            configManager.reload();
        }
        if (localizationManager != null) {
            localizationManager.reload();
        }
    }
}
