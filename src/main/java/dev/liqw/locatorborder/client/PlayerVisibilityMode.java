package dev.liqw.locatorborder.client;

public enum PlayerVisibilityMode {

    ALL,
    SAME_TEAM;

    public static PlayerVisibilityMode parse(String value) {
        return valueOf(ConfigEnum.normalize(value, "Player visibility cannot be null"));
    }
}
