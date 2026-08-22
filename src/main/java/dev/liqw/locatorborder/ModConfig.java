package dev.liqw.locatorborder;

import java.io.File;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import dev.liqw.locatorborder.client.ColorSource;
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
        String focusTriggerName = config.getString(
            "focusTrigger",
            "waypoint",
            focusTrigger.name(),
            "Controls when waypoint labels and focused sizing are revealed.",
            FocusTrigger.validNames());
        focusTrigger = FocusTrigger.parse(focusTriggerName);
        playerFaces = config.getBoolean("playerFaces", "waypoint", playerFaces, "Render player skin faces.");
        String colorSourceName = config.getString(
            "colorSource",
            "waypoint",
            colorSource.name(),
            "Marker color source: per-player UUID hash or ServerUtilities team color.",
            ColorSource.validNames());
        colorSource = ColorSource.parse(colorSourceName);
        String showPlayersName = config.getString(
            "showPlayers",
            "waypoint",
            showPlayers.name(),
            "Which players' markers are shown: everyone or only players on your team.",
            PlayerVisibilityMode.validNames());
        showPlayers = PlayerVisibilityMode.parse(showPlayersName);
        String playerNameDisplayName = config.getString(
            "playerNameDisplay",
            "waypoint",
            playerNameDisplay.name(),
            "Controls when the player name label is revealed.",
            LabelDisplay.validNames());
        playerNameDisplay = LabelDisplay.parse(playerNameDisplayName);
        String distanceDisplayName = config.getString(
            "distanceDisplay",
            "waypoint",
            distanceDisplay.name(),
            "Controls when the distance label is revealed.",
            LabelDisplay.validNames());
        distanceDisplay = LabelDisplay.parse(distanceDisplayName);
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
