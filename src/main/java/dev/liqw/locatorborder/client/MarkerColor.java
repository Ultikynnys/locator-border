package dev.liqw.locatorborder.client;

import java.util.UUID;

final class MarkerColor {

    private MarkerColor() {}

    static int fromPlayer(UUID id) {
        if (id == null) throw new IllegalArgumentException("Player id cannot be null");
        int hash = id.hashCode();
        int red = brighten(hash >> 16 & 255);
        int green = brighten(hash >> 8 & 255);
        int blue = brighten(hash & 255);
        return red << 16 | green << 8 | blue;
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
