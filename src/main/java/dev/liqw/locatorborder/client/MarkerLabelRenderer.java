package dev.liqw.locatorborder.client;

import net.minecraft.client.gui.FontRenderer;

import dev.liqw.locatorborder.ModConfig;

final class MarkerLabelRenderer {

    private MarkerLabelRenderer() {}

    static String text(String name, double distance, float focusProgress) {
        boolean focused = focusProgress > 0.0F;
        boolean showName = ModConfig.playerNameDisplay.reveals(focused);
        boolean showDistance = ModConfig.distanceDisplay.reveals(focused);
        if (!showName && !showDistance) return null;
        if (!showName) return distance(distance);
        return showDistance ? name + " " + distance(distance) : name;
    }

    static String distance(double distance) {
        return (int) distance + "m";
    }

    static void drawOutlined(FontRenderer fontRenderer, String text, int x, int y) {
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                if (offsetX != 0 || offsetY != 0) {
                    fontRenderer.drawString(text, x + offsetX, y + offsetY, 0xFF000000);
                }
            }
        }
        fontRenderer.drawString(text, x, y, 0xFFFFFFFF);
    }
}
