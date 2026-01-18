package com.dnocturne.basalt.ui;

import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.basalt.util.MessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Reusable action bar display utility for showing resource bars.
 *
 * <p>Can be used by any plugin or system that needs to display
 * a resource bar in the action bar (blood, mana, rage, stamina, etc.).</p>
 *
 * <p>Example usage with LocalizationManager:</p>
 * <pre>{@code
 * ActionBarDisplay display = ActionBarDisplay.builder()
 *     .localizationManager(localizationManager)
 *     .formatKey("vampirism.action-bar.format")
 *     .barConfigKey("vampirism.action-bar.bar")
 *     .updateInterval(1)
 *     .onlyOnChange(false)
 *     .build();
 *
 * // In tick method:
 * display.update(player, currentValue, maxValue);
 * }</pre>
 */
public class ActionBarDisplay {

    private final @Nullable Supplier<LocalizationManager> localizationSupplier;
    private final String formatKey;
    private final BarConfig barConfig;
    private final int updateInterval;
    private final boolean onlyOnChange;

    // State tracking
    private int tickCounter = 0;
    private double lastValue = -1;

    private ActionBarDisplay(Builder builder) {
        this.localizationSupplier = builder.localizationSupplier;
        this.formatKey = builder.formatKey;
        this.barConfig = builder.barConfig;
        this.updateInterval = builder.updateInterval;
        this.onlyOnChange = builder.onlyOnChange;
    }

    /**
     * Create a new builder for ActionBarDisplay.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Update the action bar display. Call this every tick.
     *
     * @param player    The player to show the action bar to
     * @param current   Current resource value
     * @param max       Maximum resource value
     * @param resolvers Additional tag resolvers for the format
     * @return true if the action bar was updated this tick
     */
    public boolean update(@NotNull Player player, double current, double max, TagResolver... resolvers) {
        tickCounter++;

        if (tickCounter < updateInterval) {
            return false;
        }

        tickCounter = 0;

        // Check if we should only show on change
        if (onlyOnChange && Math.abs(lastValue - current) < 0.01) {
            return false;
        }

        lastValue = current;

        // Build and send
        send(player, current, max, resolvers);
        return true;
    }

    /**
     * Force send the action bar immediately, bypassing interval and change checks.
     *
     * @param player    The player to show the action bar to
     * @param current   Current resource value
     * @param max       Maximum resource value
     * @param resolvers Additional tag resolvers for the format
     */
    public void send(@NotNull Player player, double current, double max, TagResolver... resolvers) {
        LocalizationManager lang = localizationSupplier != null ? localizationSupplier.get() : null;
        if (lang == null) {
            return;
        }

        // Build the visual bar
        String bar = barConfig.build(current, max, lang);

        // Calculate percentage
        double percent = max > 0 ? (current / max) * 100.0 : 0;

        // Combine resolvers
        TagResolver[] allResolvers = new TagResolver[resolvers.length + 4];
        allResolvers[0] = MessageUtil.placeholder("value", String.format("%.0f", current));
        allResolvers[1] = MessageUtil.placeholder("max", String.format("%.0f", max));
        allResolvers[2] = MessageUtil.placeholder("percent", String.format("%.0f", percent));
        allResolvers[3] = MessageUtil.placeholder("bar", bar);
        System.arraycopy(resolvers, 0, allResolvers, 4, resolvers.length);

        lang.sendActionBar(player, formatKey, allResolvers);
    }

    /**
     * Reset the tick counter and last value tracking.
     * Call this when the display is first applied or needs to be reset.
     */
    public void reset() {
        tickCounter = 0;
        lastValue = -1;
    }

    /**
     * Get the update interval.
     */
    public int getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Check if only-on-change mode is enabled.
     */
    public boolean isOnlyOnChange() {
        return onlyOnChange;
    }

    /**
     * Configuration for the visual bar portion of the action bar.
     */
    public static class BarConfig {
        private final String configKeyPrefix;

        // Cached values (loaded on first use)
        private String filledChar;
        private String emptyChar;
        private String filledColorHigh;
        private String filledColorMid;
        private String filledColorLow;
        private String emptyColor;
        private int segments;
        private final double midThreshold;
        private final double lowThreshold;

        /**
         * Create a bar config that reads settings from a lang file key prefix.
         *
         * <p>Expected keys under the prefix:</p>
         * <ul>
         *   <li>{prefix}.filled - Character for filled segments</li>
         *   <li>{prefix}.empty - Character for empty segments</li>
         *   <li>{prefix}.filled-color-high - Color when above mid threshold</li>
         *   <li>{prefix}.filled-color-mid - Color between low and mid threshold</li>
         *   <li>{prefix}.filled-color-low - Color below low threshold</li>
         *   <li>{prefix}.empty-color - Color for empty segments</li>
         *   <li>{prefix}.segments - Number of bar segments</li>
         * </ul>
         *
         * @param configKeyPrefix The lang file key prefix (e.g., "vampirism.action-bar.bar")
         */
        public BarConfig(@NotNull String configKeyPrefix) {
            this(configKeyPrefix, 0.5, 0.25);
        }

        /**
         * Create a bar config with custom thresholds.
         *
         * @param configKeyPrefix The lang file key prefix
         * @param midThreshold    Percentage threshold for mid color (0.0-1.0)
         * @param lowThreshold    Percentage threshold for low color (0.0-1.0)
         */
        public BarConfig(@NotNull String configKeyPrefix, double midThreshold, double lowThreshold) {
            this.configKeyPrefix = configKeyPrefix;
            this.midThreshold = midThreshold;
            this.lowThreshold = lowThreshold;
        }

        /**
         * Build the visual bar string.
         *
         * @param current Current value
         * @param max     Maximum value
         * @param lang    Localization manager for loading config
         * @return The formatted bar string (MiniMessage format)
         */
        public String build(double current, double max, @NotNull LocalizationManager lang) {
            loadConfigIfNeeded(lang);

            // Calculate filled segments
            double percent = max > 0 ? current / max : 0;
            int filledCount = (int) Math.round(percent * segments);

            // Determine fill color based on percentage
            String fillColor;
            if (percent > midThreshold) {
                fillColor = filledColorHigh;
            } else if (percent > lowThreshold) {
                fillColor = filledColorMid;
            } else {
                fillColor = filledColorLow;
            }

            // Build the bar
            StringBuilder bar = new StringBuilder();
            bar.append(fillColor);
            for (int i = 0; i < filledCount; i++) {
                bar.append(filledChar);
            }
            bar.append(emptyColor);
            for (int i = filledCount; i < segments; i++) {
                bar.append(emptyChar);
            }

            return bar.toString();
        }

        private void loadConfigIfNeeded(@NotNull LocalizationManager lang) {
            if (filledChar != null) return; // Already loaded

            filledChar = getOrDefault(lang, configKeyPrefix + ".filled", "▌");
            emptyChar = getOrDefault(lang, configKeyPrefix + ".empty", "▌");
            filledColorHigh = getOrDefault(lang, configKeyPrefix + ".filled-color-high", "<#55ff55>");
            filledColorMid = getOrDefault(lang, configKeyPrefix + ".filled-color-mid", "<#ffaa00>");
            filledColorLow = getOrDefault(lang, configKeyPrefix + ".filled-color-low", "<#ff5555>");
            emptyColor = getOrDefault(lang, configKeyPrefix + ".empty-color", "<#3d3d3d>");

            String segmentsStr = lang.getRaw(configKeyPrefix + ".segments");
            segments = 10;
            if (!segmentsStr.equals(configKeyPrefix + ".segments")) {
                try {
                    segments = Integer.parseInt(segmentsStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        private String getOrDefault(@NotNull LocalizationManager lang, String key, String defaultValue) {
            String value = lang.getRaw(key);
            return value.equals(key) ? defaultValue : value;
        }

        /**
         * Force reload of configuration on next build.
         */
        public void invalidateCache() {
            filledChar = null;
        }
    }

    /**
     * Builder for ActionBarDisplay.
     */
    public static class Builder {
        private @Nullable Supplier<LocalizationManager> localizationSupplier;
        private String formatKey = "action-bar.format";
        private BarConfig barConfig = new BarConfig("action-bar.bar");
        private int updateInterval = 1;
        private boolean onlyOnChange = false;

        /**
         * Set the localization manager supplier.
         *
         * <p>Using a supplier allows lazy retrieval of the manager,
         * which is useful when the manager may not be available
         * at construction time.</p>
         *
         * @param supplier Supplier that provides the LocalizationManager
         */
        public Builder localizationSupplier(@NotNull Supplier<LocalizationManager> supplier) {
            this.localizationSupplier = supplier;
            return this;
        }

        /**
         * Set the localization manager directly.
         *
         * @param manager The LocalizationManager instance
         */
        public Builder localizationManager(@NotNull LocalizationManager manager) {
            this.localizationSupplier = () -> manager;
            return this;
        }

        /**
         * Set the lang file key for the action bar format.
         *
         * <p>The format can use these placeholders:</p>
         * <ul>
         *   <li>{@code <value>} - Current value</li>
         *   <li>{@code <max>} - Maximum value</li>
         *   <li>{@code <percent>} - Percentage (0-100)</li>
         *   <li>{@code <bar>} - Visual bar</li>
         * </ul>
         *
         * @param key The lang file key (e.g., "vampirism.action-bar.format")
         */
        public Builder formatKey(@NotNull String key) {
            this.formatKey = key;
            return this;
        }

        /**
         * Set the lang file key prefix for bar configuration.
         *
         * @param keyPrefix The key prefix (e.g., "vampirism.action-bar.bar")
         */
        public Builder barConfigKey(@NotNull String keyPrefix) {
            this.barConfig = new BarConfig(keyPrefix);
            return this;
        }

        /**
         * Set a custom bar configuration.
         *
         * @param config The bar configuration
         */
        public Builder barConfig(@NotNull BarConfig config) {
            this.barConfig = config;
            return this;
        }

        /**
         * Set how often the action bar updates (in update calls).
         *
         * <p>This counts how many times {@link ActionBarDisplay#update} is called
         * before actually sending the action bar.</p>
         *
         * @param interval Update interval (1 = every update call)
         */
        public Builder updateInterval(int interval) {
            this.updateInterval = Math.max(1, interval);
            return this;
        }

        /**
         * Set whether to only show the action bar when the value changes.
         *
         * @param onlyOnChange true to only update on change
         */
        public Builder onlyOnChange(boolean onlyOnChange) {
            this.onlyOnChange = onlyOnChange;
            return this;
        }

        /**
         * Build the ActionBarDisplay.
         */
        public ActionBarDisplay build() {
            return new ActionBarDisplay(this);
        }
    }
}
