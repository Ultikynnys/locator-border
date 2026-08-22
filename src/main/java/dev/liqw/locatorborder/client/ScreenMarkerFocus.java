package dev.liqw.locatorborder.client;

final class ScreenMarkerFocus {

    private ScreenMarkerFocus() {}

    static boolean hover(float mouseX, float mouseY, ScreenEdgeProjection.Point point, float width, float height) {
        return mouseX >= point.x - width * 0.5F && mouseX <= point.x + width * 0.5F
            && mouseY >= point.y - height * 0.5F
            && mouseY <= point.y + height * 0.5F;
    }
}
