package dev.liqw.locatorborder;

import java.util.regex.Pattern;

public final class MarkerColorFormat {

    private static final String HEX_DIGITS = "[0-9A-Fa-f]{6}";
    private static final Pattern CONFIG_PATTERN = Pattern.compile("#?" + HEX_DIGITS);

    private MarkerColorFormat() {}

    public static int parse(String value) {
        if (value == null) throw new IllegalArgumentException("Marker color cannot be null");
        String normalized = value.trim();
        if (normalized.startsWith("#")) normalized = normalized.substring(1);
        if (!normalized.matches(HEX_DIGITS)) {
            throw new IllegalArgumentException("Marker color must contain exactly six hexadecimal digits");
        }
        return Integer.parseInt(normalized, 16) & 0xFFFFFF;
    }

    public static Pattern configPattern() {
        return CONFIG_PATTERN;
    }
}
