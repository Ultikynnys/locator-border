package dev.liqw.locatorborder.client;

public enum FocusTrigger {
    HOVER,
    FOCAL,
    PLAYER_LIST,
    ALWAYS,
    NONE;

    private static final String[] NAMES = names();

    public boolean reveals(boolean aimed, boolean playerListPressed) {
        switch (this) {
            case ALWAYS:
                return true;
            case HOVER:
            case FOCAL:
                return aimed;
            case PLAYER_LIST:
                return playerListPressed;
            case NONE:
                return false;
            default:
                throw new IllegalStateException("Unhandled focus trigger: " + this);
        }
    }

    public static FocusTrigger parse(String value) {
        if (value == null) throw new IllegalArgumentException("Focus trigger cannot be null");
        return valueOf(value.trim()
            .toUpperCase());
    }

    public static String[] validNames() {
        return NAMES.clone();
    }

    private static String[] names() {
        FocusTrigger[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) names[i] = values[i].name();
        return names;
    }
}
