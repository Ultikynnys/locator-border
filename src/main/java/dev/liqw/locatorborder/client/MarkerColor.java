package dev.liqw.locatorborder.client;

import dev.liqw.locatorborder.MarkerColorFormat;

final class MarkerColor {

    private MarkerColor() {}

    static int parse(String value) {
        return MarkerColorFormat.parse(value);
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
