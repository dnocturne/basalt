package com.dnocturne.basalt.locale;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages localization/translations for multiple languages using BoostedYAML.
 */
public class LocalizationManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Plugin plugin;
    private final Logger logger;
    private final File langFolder;
    private final Map<String, YamlDocument> loadedLanguages = new HashMap<>();

    private String defaultLanguage = "en";
    private String[] availableLanguages = {"en"};
    private @Nullable Supplier<String> languageConfigSupplier;
    private @Nullable YamlDocument currentLanguage;
    private @Nullable String currentLanguageCode;

    public LocalizationManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.langFolder = new File(plugin.getDataFolder(), "lang");
    }

    public LocalizationManager availableLanguages(@NotNull String... languages) {
        this.availableLanguages = languages;
        return this;
    }

    public LocalizationManager defaultLanguage(@NotNull String language) {
        this.defaultLanguage = language;
        return this;
    }

    public LocalizationManager languageFromConfig(@NotNull Supplier<String> supplier) {
        this.languageConfigSupplier = supplier;
        return this;
    }

    public void load() {
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        extractDefaultLanguages();
        String configuredLang = languageConfigSupplier != null
                ? languageConfigSupplier.get()
                : defaultLanguage;
        setLanguage(configuredLang);
    }

    private void extractDefaultLanguages() {
        for (String lang : availableLanguages) {
            String resourcePath = "lang/" + lang + ".yml";
            InputStream resource = plugin.getResource(resourcePath);
            if (resource != null) {
                File langFile = new File(langFolder, lang + ".yml");
                if (!langFile.exists()) {
                    try {
                        YamlDocument.create(langFile, resource,
                                GeneralSettings.DEFAULT,
                                LoaderSettings.builder().setAutoUpdate(true).build(),
                                DumperSettings.DEFAULT,
                                UpdaterSettings.DEFAULT
                        );
                        logger.info("Extracted language file: " + lang + ".yml");
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Failed to extract language file: " + lang, e);
                    }
                }
            }
        }
    }

    public boolean setLanguage(@NotNull String languageCode) {
        try {
            YamlDocument lang = loadLanguage(languageCode);
            if (lang != null) {
                currentLanguage = lang;
                currentLanguageCode = languageCode;
                logger.info("Language set to: " + languageCode);
                return true;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load language: " + languageCode, e);
        }
        if (!languageCode.equals(defaultLanguage)) {
            logger.warning("Falling back to default language: " + defaultLanguage);
            return setLanguage(defaultLanguage);
        }
        return false;
    }

    private @Nullable YamlDocument loadLanguage(@NotNull String languageCode) throws IOException {
        if (loadedLanguages.containsKey(languageCode)) {
            return loadedLanguages.get(languageCode);
        }
        File langFile = new File(langFolder, languageCode + ".yml");
        InputStream defaultResource = plugin.getResource("lang/" + languageCode + ".yml");
        if (!langFile.exists() && defaultResource == null) {
            logger.warning("Language file not found: " + languageCode + ".yml");
            return null;
        }
        YamlDocument doc;
        if (defaultResource != null) {
            doc = YamlDocument.create(langFile, defaultResource,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
        } else {
            doc = YamlDocument.create(langFile,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
        }
        loadedLanguages.put(languageCode, doc);
        return doc;
    }

    public void reload() {
        loadedLanguages.clear();
        load();
    }

    public @NotNull String getRaw(@NotNull String key) {
        if (currentLanguage == null) return key;
        return currentLanguage.getString(key, key);
    }

    public @NotNull Component get(@NotNull String key) {
        return MINI_MESSAGE.deserialize(getRaw(key));
    }

    public @NotNull Component get(@NotNull String key, @NotNull TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(getRaw(key), resolvers);
    }

    public @NotNull Component get(@NotNull String key, @NotNull Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((k, v) -> builder.resolver(Placeholder.parsed(k, v)));
        return MINI_MESSAGE.deserialize(getRaw(key), builder.build());
    }

    public @NotNull Component getPrefixed(@NotNull String key) {
        return get("prefix").append(get(key));
    }

    public @NotNull Component getPrefixed(@NotNull String key, @NotNull TagResolver... resolvers) {
        return get("prefix").append(get(key, resolvers));
    }

    public void send(@NotNull CommandSender sender, @NotNull String key) {
        sender.sendMessage(getPrefixed(key));
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull TagResolver... resolvers) {
        sender.sendMessage(getPrefixed(key, resolvers));
    }

    public void sendRaw(@NotNull CommandSender sender, @NotNull String key) {
        sender.sendMessage(get(key));
    }

    public void sendRaw(@NotNull CommandSender sender, @NotNull String key, @NotNull TagResolver... resolvers) {
        sender.sendMessage(get(key, resolvers));
    }

    public void sendActionBar(@NotNull Player player, @NotNull String key) {
        player.sendActionBar(get(key));
    }

    public void sendActionBar(@NotNull Player player, @NotNull String key, @NotNull TagResolver... resolvers) {
        player.sendActionBar(get(key, resolvers));
    }

    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull String value) {
        return Placeholder.parsed(key, value);
    }

    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull Component value) {
        return Placeholder.component(key, value);
    }

    public @Nullable String getCurrentLanguageCode() {
        return currentLanguageCode;
    }

    public @NotNull String[] getAvailableLanguages() {
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return new String[0];
        String[] langs = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            langs[i] = files[i].getName().replace(".yml", "");
        }
        return langs;
    }

    public static @NotNull MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }

    public @Nullable YamlDocument getCurrentLanguageDocument() {
        return currentLanguage;
    }
}
