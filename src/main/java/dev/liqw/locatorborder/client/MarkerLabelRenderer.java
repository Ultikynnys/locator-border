package dev.liqw.locatorborder.client;

import net.minecraft.client.gui.FontRenderer;

final class MarkerLabelRenderer {

    private MarkerLabelRenderer() {}

    static String text(String name, String distance, boolean revealDistance, boolean displayDistance) {
        return revealDistance && displayDistance ? name + " " + distance : name;
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
