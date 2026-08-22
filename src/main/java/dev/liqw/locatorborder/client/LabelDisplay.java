package dev.liqw.locatorborder.client;

public enum LabelDisplay {

    LOOK_AT,
    ALWAYS,
    NONE;

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
        String normalized = ConfigEnum.normalize(value, "Label display cannot be null");
        if ("PLAYERNAME".equals(normalized) || "DISTANCE".equals(normalized) || "TRUE".equals(normalized)) {
            return LOOK_AT;
        }
        if ("FALSE".equals(normalized)) return NONE;
        return valueOf(normalized);
    }
}
