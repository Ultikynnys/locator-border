package dev.liqw.locatorborder;

import java.io.File;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import dev.liqw.locatorborder.client.FocusTrigger;

public final class ModConfig {

    public static final String[] CLIENT_CATEGORIES = { "waypoint", "network" };

    private static File configFile;
    private static Configuration configuration;

    public static int updateIntervalTicks = 10;
    public static boolean sameDimensionOnly = true;
    public static int maximumPlayersPerSnapshot = 256;

    public static float unfocusedScale = 1.0F;
    public static float focusedScale = 1.2F;
    public static FocusTrigger focusTrigger = FocusTrigger.HOVER;
    public static boolean playerFaces = false;
    public static String colorSource = "WAYPOINT";
    public static boolean displayPlayerName = true;
    public static boolean displayDistance = false;
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
        String focusTriggerName = config.getString(
            "focusTrigger",
            "waypoint",
            focusTrigger.name(),
            "Controls when waypoint labels and focused sizing are revealed.",
            FocusTrigger.validNames());
        focusTrigger = FocusTrigger.parse(focusTriggerName);
        playerFaces = config.getBoolean("playerFaces", "waypoint", playerFaces, "Render player skin faces.");
        colorSource = enumValue(config, "colorSource", "waypoint", colorSource, "WAYPOINT", "TEAM");
        displayPlayerName = config
            .getBoolean("displayPlayerName", "waypoint", displayPlayerName, "Display the player name while focused.");
        displayDistance = config.getBoolean(
            "displayDistance",
            "waypoint",
            displayDistance,
            "Display distance while looking at a player marker.");
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
            "distanceScale");
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

    private static String enumValue(Configuration config, String key, String category, String fallback,
        String... valid) {
        String value = config.getString(key, category, fallback, "Allowed values: " + join(valid))
            .toUpperCase();
        for (String candidate : valid) {
            if (candidate.equals(value)) return value;
        }
        throw new IllegalArgumentException("Invalid " + category + "." + key + " value: " + value);
    }

    private static String join(String[] values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) result.append(", ");
            result.append(values[i]);
        }
        return result.toString();
    }

    private static void removeCategoryIfEmpty(Configuration config, String categoryName) {
        if (config.hasCategory(categoryName) && config.getCategory(categoryName)
            .isEmpty()) {
            config.removeCategory(config.getCategory(categoryName));
        }
    }
}
