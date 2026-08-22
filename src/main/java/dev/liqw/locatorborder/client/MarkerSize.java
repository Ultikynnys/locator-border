package dev.liqw.locatorborder.client;

import dev.liqw.locatorborder.ModConfig;

final class MarkerSize {

    static final float HALF_SIZE = 3.5F;
    private static final float MINIMUM_DISTANCE_SCALE = 0.02F;
    private static final float DISTANCE_SCALE_PER_BLOCK = 0.002F;
    private static final double DISTANCE_SCALE_THRESHOLD = 10.0D;

    private MarkerSize() {}

    static float scale(float focusProgress) {
        float progress = Math.max(0.0F, Math.min(1.0F, focusProgress));
        return ModConfig.unfocusedScale + (ModConfig.focusedScale - ModConfig.unfocusedScale) * progress;
    }

    // World-space half-size of the waypoint (blocks). Player distance and the
    // focus state are the only two factors that change the scale; both world
    // and screen-edge indicators derive their size from here.
    static float worldHalfSize(double distance, float focusProgress) {
        return HALF_SIZE * scale(focusProgress) * distanceScale(distance);
    }

    // Distance scaling shared by world and screen-edge indicators: markers shrink
    // to a floor up close and grow with distance so they stay visible.
    static float distanceScale(double distance) {
        if (distance <= DISTANCE_SCALE_THRESHOLD) return MINIMUM_DISTANCE_SCALE;
        return (float) distance * DISTANCE_SCALE_PER_BLOCK;
    }

    // Screen-space half-size in pixels: the same world half-size projected to
    // the screen, so clamped edge dots render exactly as large as world dots.
    static float screenHalfSize(double distance, float focusProgress, float verticalFovDegrees, int screenHeight) {
        float projectedScale = (float) (screenHeight / (2.0 * Math.tan(Math.toRadians(verticalFovDegrees) * 0.5)));
        if (distance <= 0.0D) return HALF_SIZE * scale(focusProgress) * projectedScale;
        return worldHalfSize(distance, focusProgress) / (float) distance * projectedScale;
    }
}
