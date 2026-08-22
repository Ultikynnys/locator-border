package dev.liqw.locatorborder.client;

public final class ConfigEnum {

    private ConfigEnum() {}

    // Names of an enum's constants, for Forge config valid-value lists.
    public static String[] names(Class<? extends Enum<?>> type) {
        Enum<?>[] values = type.getEnumConstants();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) names[i] = values[i].name();
        return names;
    }

    // Normalizes a config value for enum lookup: rejects null and uppercases.
    public static String normalize(String value, String errorMessage) {
        if (value == null) throw new IllegalArgumentException(errorMessage);
        return value.trim()
            .toUpperCase();
    }
}
