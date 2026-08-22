package dev.liqw.locatorborder;

import java.io.File;
import java.util.function.Function;
import java.util.regex.Pattern;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import dev.liqw.locatorborder.client.ColorSource;
import dev.liqw.locatorborder.client.ConfigEnum;
import dev.liqw.locatorborder.client.FocusTrigger;
import dev.liqw.locatorborder.client.LabelDisplay;
import dev.liqw.locatorborder.client.PlayerVisibilityMode;

public final class ModConfig {

    public static final String[] CLIENT_CATEGORIES = { "waypoint", "network" };

    private static File configFile;
    private static Configuration configuration;

    public static int updateIntervalTicks = 10;
    public static boolean sameDimensionOnly = true;
    public static int maximumPlayersPerSnapshot = 256;

    public static float unfocusedScale = 1.0F;
    public static float focusedScale = 1.2F;
    public static FocusTrigger focusTrigger = FocusTrigger.LOOK_AT;
    public static boolean playerFaces = false;
    public static int waypointBorderColor = 0x000000;
    public static ColorSource colorSource = ColorSource.UUID;
    public static PlayerVisibilityMode showPlayers = PlayerVisibilityMode.ALL;
    public static LabelDisplay playerNameDisplay = LabelDisplay.LOOK_AT;
    public static LabelDisplay distanceDisplay = LabelDisplay.NONE;
    public static int staleSnapshotTicks = 100;

    private ModConfig() {}

    public static synchronized void load(File file) {
        configFile = file;
        configuration = new Configuration(file);
        reload();
    }

    public static synchronized void reload() {
        if (configuration == null) {
            if (configFile == null) throw new IllegalStateException("Locator Border config has not been initialized");
            configuration = new Configuration(configFile);
        }
        Configuration config = configuration;
        config.load();

        removeLegacyEnabledProperty(config);
        removeObsoleteProperties(config);
        updateIntervalTicks = config.getInt(
            "updateIntervalTicks",
            "server",
            updateIntervalTicks,
            1,
            200,
            "How often the server sends player positions to clients.");
        sameDimensionOnly = config.getBoolean(
            "sameDimensionOnly",
            "server",
            sameDimensionOnly,
            "Only disclose players in the recipient's current dimension.");
        maximumPlayersPerSnapshot = config.getInt(
            "maximumPlayersPerSnapshot",
            "server",
            maximumPlayersPerSnapshot,
            1,
            1024,
            "Hard limit for one snapshot packet.");

        unfocusedScale = config
            .getFloat("unfocusedScale", "waypoint", unfocusedScale, 0.25F, 4.0F, "Unfocused waypoint scale.");
        focusedScale = config
            .getFloat("focusedScale", "waypoint", focusedScale, 0.25F, 4.0F, "Focused waypoint scale.");
        focusTrigger = readEnum(
            config,
            "waypoint",
            "focusTrigger",
            focusTrigger,
            "Controls when waypoint labels and focused sizing are revealed.",
            FocusTrigger::parse);
        playerFaces = config.getBoolean("playerFaces", "waypoint", playerFaces, "Render player skin faces.");
        waypointBorderColor = readColor(
            config,
            "waypoint",
            "waypointBorderColor",
            waypointBorderColor,
            "Border color of waypoint markers, written as six hexadecimal digits (for example 000000 for black).");
        colorSource = readEnum(
            config,
            "waypoint",
            "colorSource",
            colorSource,
            "Marker color source: per-player UUID hash or ServerUtilities team color.",
            ColorSource::parse);
        showPlayers = readEnum(
            config,
            "waypoint",
            "showPlayers",
            showPlayers,
            "Which players' markers are shown: everyone or only players on your team.",
            PlayerVisibilityMode::parse);
        playerNameDisplay = readEnum(
            config,
            "waypoint",
            "playerNameDisplay",
            playerNameDisplay,
            "Controls when the player name label is revealed.",
            LabelDisplay::parse);
        distanceDisplay = readEnum(
            config,
            "waypoint",
            "distanceDisplay",
            distanceDisplay,
            "Controls when the distance label is revealed.",
            LabelDisplay::parse);
        staleSnapshotTicks = config.getInt(
            "staleSnapshotTicks",
            "network",
            staleSnapshotTicks,
            20,
            1200,
            "Discard server waypoint data after this many client ticks without an update.");

        if (config.hasChanged()) config.save();
    }

    public static synchronized Configuration getConfiguration() {
        if (configuration == null) throw new IllegalStateException("Locator Border config has not been initialized");
        return configuration;
    }

    public static synchronized String getConfigPath() {
        if (configFile == null) throw new IllegalStateException("Locator Border config has not been initialized");
        return configFile.getAbsolutePath();
    }

    // Reads an enum property: Forge validates against the enum's constant names,
    // then the enum's own parse applies any legacy aliases.
    private static <T extends Enum<T>> T readEnum(Configuration config, String category, String key, T defaultValue,
        String comment, Function<String, T> parser) {
        String name = config
            .getString(key, category, defaultValue.name(), comment, ConfigEnum.names(defaultValue.getDeclaringClass()));
        return parser.apply(name);
    }

    // Reads a hex color like "55FFFF" (optionally '#'-prefixed), falling back
    // to the previous value when the stored text is not valid hex.
    private static int readColor(Configuration config, String category, String key, int fallback, String comment) {
        String defaultValue = String.format("%06X", fallback);
        String stored = config.getString(key, category, defaultValue, comment);
        config.getCategory(category)
            .get(key)
            .setValidationPattern(Pattern.compile("#?[0-9A-Fa-f]{6}"));
        try {
            String hex = stored.trim();
            if (hex.startsWith("#")) hex = hex.substring(1);
            if (hex.length() != 6) throw new NumberFormatException("expected six hex digits");
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException invalid) {
            LocatorBorder.LOG.warn("Invalid {}.{} value '{}'; using default {}.", category, key, stored, defaultValue);
            return fallback;
        }
    }

    private static void removeLegacyEnabledProperty(Configuration config) {
        if (!config.hasCategory("general") || !config.getCategory("general")
            .containsKey("enabled")) return;
        boolean legacyEnabled = config.getCategory("general")
            .get("enabled")
            .getBoolean(true);
        config.getCategory("general")
            .remove("enabled");
        LocatorBorder.LOG.info(
            "Removed legacy general.enabled={}; locator visibility now starts enabled and is controlled by its keybinding.",
            legacyEnabled);
        removeCategoryIfEmpty(config, "general");
    }

    private static void removeObsoleteProperties(Configuration config) {
        removeProperties(
            config,
            "waypoint",
            "scale",
            "inset",
            "markerColor",
            "outlineColor",
            "outlineStyle",
            "animations",
            "directionArrows",
            "distanceScale",
            "displayPlayerName",
            "displayDistance");
        removeProperties(config, "focus", "scale", "focusTrigger", "displayPlayerName", "displayDistance", "inset");
        if (config.hasCategory("compass")) {
            config.removeCategory(config.getCategory("compass"));
            LocatorBorder.LOG.info("Removed unsupported compass configuration category.");
        }
    }

    private static void removeProperties(Configuration config, String categoryName, String... keys) {
        if (!config.hasCategory(categoryName)) return;
        ConfigCategory category = config.getCategory(categoryName);
        for (String key : keys) {
            if (category.remove(key) != null) {
                LocatorBorder.LOG.info("Removed unsupported {}.{} configuration property.", categoryName, key);
            }
        }
        removeCategoryIfEmpty(config, categoryName);
    }

    private static void removeCategoryIfEmpty(Configuration config, String categoryName) {
        if (config.hasCategory(categoryName) && config.getCategory(categoryName)
            .isEmpty()) {
            config.removeCategory(config.getCategory(categoryName));
        }
    }
}
