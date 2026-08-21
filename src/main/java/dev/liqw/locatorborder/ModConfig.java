package dev.liqw.locatorborder;

import java.io.File;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import dev.liqw.locatorborder.client.FocusTrigger;

public final class ModConfig {

    public static final String[] CLIENT_CATEGORIES = { "waypoint", "focus", "network" };

    private static File configFile;
    private static Configuration configuration;

    public static int updateIntervalTicks = 10;
    public static boolean sameDimensionOnly = true;
    public static int maximumPlayersPerSnapshot = 256;

    public static int inset = 4;
    public static float waypointScale = 1.0F;
    public static boolean playerFaces = false;
    public static String markerColor = "55FFFF";
    public static FocusTrigger focusTrigger = FocusTrigger.HOVER;
    public static float focusScale = 1.2F;
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
        removeUnsupportedClientProperties(config);
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

        inset = config.getInt("inset", "waypoint", inset, 0, 100, "Distance from the screen edge in pixels.");
        waypointScale = config.getFloat(
            "scale",
            "waypoint",
            waypointScale,
            0.25F,
            4.0F,
            "Unfocused waypoint scale.");
        playerFaces = config.getBoolean("playerFaces", "waypoint", playerFaces, "Render player skin faces.");
        markerColor = config.getString(
            "markerColor",
            "waypoint",
            markerColor,
            "Shared RGB marker color for every player, written as six hexadecimal digits (for example 55FFFF).");
        config.getCategory("waypoint")
            .get("markerColor")
            .setValidationPattern(MarkerColorFormat.configPattern());
        String focusTriggerName = config.getString(
            "focusTrigger",
            "focus",
            focusTrigger.name(),
            "Controls when waypoint labels and focused sizing are revealed.",
            FocusTrigger.validNames());
        focusTrigger = FocusTrigger.parse(focusTriggerName);
        focusScale = config.getFloat("scale", "focus", focusScale, 0.25F, 4.0F, "Focused waypoint scale.");
        displayDistance = config.getBoolean(
            "displayDistance",
            "focus",
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

    private static void removeUnsupportedClientProperties(Configuration config) {
        removeProperties(
            config,
            "waypoint",
            "animations",
            "directionArrows",
            "distanceScale",
            "colorSource",
            "outlineStyle",
            "outlineColor");
        removeProperties(config, "focus", "inset", "displayPlayerName");
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
