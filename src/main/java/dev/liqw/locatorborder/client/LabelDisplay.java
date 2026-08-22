package dev.liqw.locatorborder.client;

public enum LabelDisplay {

    LOOK_AT,
    ALWAYS,
    NONE;

    private static final String[] NAMES = names();

    public boolean reveals(boolean focused) {
        switch (this) {
            case LOOK_AT:
                return focused;
            case ALWAYS:
                return true;
            case NONE:
                return false;
            default:
                throw new IllegalStateException("Unhandled label display mode: " + this);
        }
    }

    public static LabelDisplay parse(String value) {
        if (value == null) throw new IllegalArgumentException("Label display cannot be null");
        String normalized = value.trim()
            .toUpperCase();
        if ("PLAYERNAME".equals(normalized) || "DISTANCE".equals(normalized) || "TRUE".equals(normalized)) {
            return LOOK_AT;
        }
        if ("FALSE".equals(normalized)) return NONE;
        return valueOf(normalized);
    }

    public static String[] validNames() {
        return NAMES.clone();
    }

    private static String[] names() {
        LabelDisplay[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) names[i] = values[i].name();
        return names;
    }
}
