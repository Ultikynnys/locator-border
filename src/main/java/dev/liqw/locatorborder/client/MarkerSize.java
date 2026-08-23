package dev.liqw.locatorborder.client;

import dev.liqw.locatorborder.ModConfig;

final class MarkerSize {

    static final float HALF_SIZE = 3.5F;

    // Fixed reference FOV (35-degree half angle) used for sizing so the waypoint
    // size never depends on the player's current FOV setting. The in-world marker
    // compensates for the actual render FOV so both stay the same on-screen size.
    static final float REFERENCE_TAN_HALF_FOV = (float) Math.tan(Math.toRadians(35.0D));

    // tan(half of the render FOV), captured each world render from the projection
    // matrix; defaults to the reference so sizing is stable before first capture.
    static float renderTanHalfFov = REFERENCE_TAN_HALF_FOV;

    private static final float MINIMUM_DISTANCE_SCALE = 0.02F;
    private static final float DISTANCE_SCALE_PER_BLOCK = 0.002F;
    private static final double DISTANCE_SCALE_THRESHOLD = 10.0D;

    private MarkerSize() {}

    static float scale(float focusProgress) {
        float progress = Math.max(0.0F, Math.min(1.0F, focusProgress));
        return ModConfig.unfocusedScale + (ModConfig.focusedScale - ModConfig.unfocusedScale) * progress;
    }

    // World-space half-size of the waypoint (blocks). Player distance and focus
    // state are the only factors that change the scale; both world and screen-edge
    // indicators derive their size from here.
    static float worldHalfSize(double distance, float focusProgress) {
        return HALF_SIZE * scale(focusProgress) * distanceScale(distance);
    }

    // Distance scaling shared by world and screen-edge indicators: markers shrink
    // to a floor up close and grow with distance so they stay visible.
    static float distanceScale(double distance) {
        if (distance <= DISTANCE_SCALE_THRESHOLD) return MINIMUM_DISTANCE_SCALE;
        return (float) distance * DISTANCE_SCALE_PER_BLOCK;
    }

    // Screen-space half-size in pixels, independent of FOV: a fixed reference
    // projection is used so changing the player's FOV does not resize the marker.
    static float screenHalfSize(double distance, float focusProgress, int screenHeight) {
        float projectedScale = screenHeight / (2.0F * REFERENCE_TAN_HALF_FOV);
        if (distance <= 0.0D) return HALF_SIZE * scale(focusProgress) * projectedScale;
        return worldHalfSize(distance, focusProgress) / (float) distance * projectedScale;
    }

    // Compensates the world-space marker for the actual render FOV so it appears at
    // exactly screenHalfSize() pixels regardless of the player's FOV.
    static float worldRenderScale() {
        return renderTanHalfFov / REFERENCE_TAN_HALF_FOV;
    }
}
