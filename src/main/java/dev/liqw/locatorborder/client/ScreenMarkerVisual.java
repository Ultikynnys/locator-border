package dev.liqw.locatorborder.client;

final class ScreenMarkerVisual {

    private static final float HOTBAR_WIDTH = 206.0F;
    private static final float HOTBAR_FADE_BUFFER = 48.0F;

    private ScreenMarkerVisual() {}

    static int inset(int baseInset, int focusInset, float focusProgress) {
        return baseInset + Math.round(focusInset * clamp(focusProgress));
    }

    static float alpha(float x, float y, int width, int height, boolean animated) {
        if (y <= height * 0.5F) return 1.0F;
        float distance = Math.abs(x - width * 0.5F) - HOTBAR_WIDTH * 0.5F;
        if (!animated) return distance > 0.0F ? 1.0F : 0.0F;
        return clamp(distance / HOTBAR_FADE_BUFFER);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
