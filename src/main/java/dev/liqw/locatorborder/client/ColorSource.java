package dev.liqw.locatorborder.client;

public enum ColorSource {

    UUID,
    FTB_TEAM;

    public static ColorSource parse(String value) {
        String normalized = ConfigEnum.normalize(value, "Color source cannot be null");
        if ("WAYPOINT".equals(normalized)) return UUID;
        if ("TEAM".equals(normalized)) return FTB_TEAM;
        return valueOf(normalized);
    }
}
