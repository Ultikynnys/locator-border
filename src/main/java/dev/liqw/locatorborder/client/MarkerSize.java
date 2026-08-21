package dev.liqw.locatorborder.client;

final class MarkerSize {

    private MarkerSize() {}

    static float scale(float focusProgress, float unfocusedScale, float focusedScale) {
        float progress = Math.max(0.0F, Math.min(1.0F, focusProgress));
        return unfocusedScale + (focusedScale - unfocusedScale) * progress;
    }
}
