package dev.liqw.locatorborder;

import java.io.File;
import java.util.regex.Pattern;

import net.minecraftforge.common.config.Configuration;

public final class ModConfig {

    public static final String[] CLIENT_CATEGORIES = { "waypoint", "focus", "compass", "network" };

    private static File configFile;
    private static Configuration configuration;

    public static int updateIntervalTicks = 10;
    public static boolean sameDimensionOnly = true;
    public static int maximumPlayersPerSnapshot = 256;

    public static int inset = 4;
    public static boolean animations = true;
    public static boolean directionArrows = false;
    public static boolean playerFaces = false;
    public static boolean distanceScale = true;
    public static String markerColor = "55FFFF";
    public static String colorSource = "WAYPOINT";
    public static String outlineStyle = "BORDER";
    public static String outlineColor = "BLACK";
    public static String focusTrigger = "HOVER";
    public static float focusScale = 1.2F;
    public static int focusInset = 2;
    public static boolean displayPlayerName = true;
    public static boolean displayDistance = false;
    public static boolean compass = false;
    public static boolean intercardinalCompass = false;
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
        animations = config.getBoolean("animations", "waypoint", animations, "Animate focus and hotbar fading.");
        directionArrows = config.getBoolean(
            "directionArrows",
            "waypoint",
            directionArrows,
            "Show an up or down marker for vertical separation.");
        playerFaces = config.getBoolean("playerFaces", "waypoint", playerFaces, "Render player skin faces.");
        distanceScale = config
            .getBoolean("distanceScale", "waypoint", distanceScale, "Scale player faces down at long distances.");
        markerColor = config.getString(
            "markerColor",
            "waypoint",
            markerColor,
            "Shared RGB marker color for every player, written as six hexadecimal digits (for example 55FFFF).");
        config.getCategory("waypoint")
            .get("markerColor")
            .setValidationPattern(Pattern.compile("#?[0-9A-Fa-f]{6}"));
        colorSource = enumValue(config, "colorSource", "waypoint", colorSource, "WAYPOINT", "TEAM");
        outlineStyle = enumValue(config, "outlineStyle", "waypoint", outlineStyle, "BORDER", "SHADOW", "NONE");
        outlineColor = enumValue(config, "outlineColor", "waypoint", outlineColor, "WAYPOINT", "TEAM", "BLACK");
        focusTrigger = enumValue(
            config,
            "focusTrigger",
            "focus",
            focusTrigger,
            "HOVER",
            "FOCAL",
            "PLAYER_LIST",
            "ALWAYS",
            "NONE");
        config.getCategory("focus")
            .get("focusTrigger")
            .setValidValues(new String[] { "HOVER", "FOCAL", "PLAYER_LIST", "ALWAYS", "NONE" });
        focusScale = config.getFloat("scale", "focus", focusScale, 0.25F, 4.0F, "Focused waypoint scale.");
        focusInset = config.getInt("inset", "focus", focusInset, 0, 100, "Additional focused waypoint inset.");
        config.getBoolean(
            "displayPlayerName",
            "focus",
            true,
            "Legacy setting. Player names are always displayed above world markers.");
        config.getCategory("focus")
            .get("displayPlayerName")
            .setShowInGui(false);
        displayPlayerName = true;
        displayDistance = config.getBoolean(
            "displayDistance",
            "focus",
            displayDistance,
            "Display distance while looking at a player marker.");
        compass = config.getBoolean("enabled", "compass", compass, "Display cardinal directions.");
        intercardinalCompass = config
            .getBoolean("intercardinal", "compass", intercardinalCompass, "Display intercardinal directions.");
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
        if (config.getCategory("general")
            .isEmpty()) config.removeCategory(config.getCategory("general"));
    }

    private static String enumValue(Configuration config, String key, String category, String fallback,
        String... valid) {
        String value = config.getString(key, category, fallback, "Allowed values: " + join(valid))
            .toUpperCase();
        for (String candidate : valid) {
            if (candidate.equals(value)) return value;
        }
        LocatorBorder.LOG
            .warn("Invalid {}.{} value '{}'; using explicit default '{}'.", category, key, value, fallback);
        return fallback;
    }

    private static String join(String[] values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) result.append(", ");
            result.append(values[i]);
        }
        return result.toString();
    }
}
