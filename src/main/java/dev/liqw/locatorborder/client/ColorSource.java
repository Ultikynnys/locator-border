package dev.liqw.locatorborder.client;

public enum ColorSource {

    UUID,
    FTB_TEAM;

    private static final String[] NAMES = names();

    public static ColorSource parse(String value) {
        if (value == null) throw new IllegalArgumentException("Color source cannot be null");
        String normalized = value.trim()
            .toUpperCase();
        if ("WAYPOINT".equals(normalized)) return UUID;
        if ("TEAM".equals(normalized)) return FTB_TEAM;
        return valueOf(normalized);
    }

    public static String[] validNames() {
        return NAMES.clone();
    }

    private static String[] names() {
        ColorSource[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) names[i] = values[i].name();
        return names;
    }
}
