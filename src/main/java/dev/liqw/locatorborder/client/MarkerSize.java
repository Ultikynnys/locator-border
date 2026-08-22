package dev.liqw.locatorborder.client;

import dev.liqw.locatorborder.ModConfig;

final class MarkerSize {

    private MarkerSize() {}

    static float scale(float focusProgress) {
        float progress = Math.max(0.0F, Math.min(1.0F, focusProgress));
        return ModConfig.unfocusedScale + (ModConfig.focusedScale - ModConfig.unfocusedScale) * progress;
    }
}
