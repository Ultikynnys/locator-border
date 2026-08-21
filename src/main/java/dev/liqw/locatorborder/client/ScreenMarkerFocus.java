package dev.liqw.locatorborder.client;

final class ScreenMarkerFocus {

    private static final double FOCAL_ANGLE_DEGREES = 15.0D;

    private ScreenMarkerFocus() {}

    static boolean hover(float mouseX, float mouseY, ScreenEdgeProjection.Point point, float width, float height) {
        return mouseX >= point.x - width * 0.5F && mouseX <= point.x + width * 0.5F
            && mouseY >= point.y - height * 0.5F
            && mouseY <= point.y + height * 0.5F;
    }

    static boolean focal(double cameraX, double cameraZ) {
        return Math.abs(Math.toDegrees(Math.atan2(cameraX, cameraZ))) < FOCAL_ANGLE_DEGREES;
    }
}
