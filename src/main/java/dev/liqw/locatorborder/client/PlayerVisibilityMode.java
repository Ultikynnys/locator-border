package dev.liqw.locatorborder.client;

public enum PlayerVisibilityMode {

    ALL,
    SAME_TEAM;

    private static final String[] NAMES = names();

    public static PlayerVisibilityMode parse(String value) {
        if (value == null) throw new IllegalArgumentException("Player visibility cannot be null");
        return valueOf(
            value.trim()
                .toUpperCase());
    }

    public static String[] validNames() {
        return NAMES.clone();
    }

    private static String[] names() {
        PlayerVisibilityMode[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) names[i] = values[i].name();
        return names;
    }
}
