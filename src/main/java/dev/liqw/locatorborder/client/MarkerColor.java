package dev.liqw.locatorborder.client;

import java.util.UUID;

final class MarkerColor {

    private static final int MINIMUM_VISIBLE_CHANNEL = 64;
    private static final int BLACK_FALLBACK = 0x808080;

    private MarkerColor() {}

    static int fromPlayer(UUID id) {
        if (id == null) throw new IllegalArgumentException("Player id cannot be null");
        int hash = id.hashCode();
        int red = brighten(hash >> 16 & 255);
        int green = brighten(hash >> 8 & 255);
        int blue = brighten(hash & 255);
        return red << 16 | green << 8 | blue;
    }

    static int ensureVisible(int color) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        int brightest = Math.max(red, Math.max(green, blue));
        if (brightest >= MINIMUM_VISIBLE_CHANNEL) return color;
        if (brightest == 0) return BLACK_FALLBACK;
        return scaleToBrightness(color, brightest);
    }

    private static int scaleToBrightness(int color, int brightest) {
        // Scale every channel up so the brightest one reaches MINIMUM_VISIBLE_CHANNEL, preserving hue.
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        return red * MINIMUM_VISIBLE_CHANNEL / brightest << 16 | green * MINIMUM_VISIBLE_CHANNEL / brightest << 8
            | blue * MINIMUM_VISIBLE_CHANNEL / brightest;
    }

    private static int brighten(int component) {
        return Math.min(255, (int) (component * 1.8F));
    }

    static float red(int color) {
        return (color >> 16 & 255) / 255.0F;
    }

    static float green(int color) {
        return (color >> 8 & 255) / 255.0F;
    }

    static float blue(int color) {
        return (color & 255) / 255.0F;
    }
}
