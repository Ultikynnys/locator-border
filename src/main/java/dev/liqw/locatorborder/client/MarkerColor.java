package dev.liqw.locatorborder.client;

final class MarkerColor {

    private MarkerColor() {}

    static int parse(String value) {
        if (value == null) throw new IllegalArgumentException("Marker color cannot be null");
        String normalized = value.trim();
        if (normalized.startsWith("#")) normalized = normalized.substring(1);
        if (!normalized.matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Marker color must contain exactly six hexadecimal digits");
        }
        return Integer.parseInt(normalized, 16) & 0xFFFFFF;
    }
}
