package dev.liqw.locatorborder.client;

final class MarkerSize {

    private MarkerSize() {}

    static float scale(boolean focused, float unfocusedScale, float focusedScale) {
        return focused ? focusedScale : unfocusedScale;
    }
}
