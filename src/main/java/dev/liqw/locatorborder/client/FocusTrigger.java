package dev.liqw.locatorborder.client;

public enum FocusTrigger {

    LOOK_AT,
    ALWAYS,
    NONE;

    public boolean reveals(boolean aimed) {
        switch (this) {
            case ALWAYS:
                return true;
            case LOOK_AT:
                return aimed;
            case NONE:
                return false;
            default:
                throw new IllegalStateException("Unhandled focus trigger: " + this);
        }
    }

    public static FocusTrigger parse(String value) {
        String normalized = ConfigEnum.normalize(value, "Focus trigger cannot be null");
        if ("HOVER".equals(normalized) || "FOCAL".equals(normalized) || "PLAYER_LIST".equals(normalized)) {
            normalized = "LOOK_AT";
        }
        return valueOf(normalized);
    }
}
