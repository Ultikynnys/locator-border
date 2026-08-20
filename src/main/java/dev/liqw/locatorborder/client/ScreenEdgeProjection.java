package dev.liqw.locatorborder.client;

final class ScreenEdgeProjection {

    private static final double EPSILON = 0.0001D;

    private ScreenEdgeProjection() {}

    static Point project(double cameraX, double cameraY, double cameraZ, float verticalFovDegrees, int width,
        int height, int inset) {
        double tanVertical = Math.tan(Math.toRadians(verticalFovDegrees) * 0.5D);
        double tanHorizontal = tanVertical * width / Math.max(1.0D, height);
        double projectedX;
        double projectedY;
        boolean behind = cameraZ <= EPSILON;
        if (behind) {
            projectedX = Math.abs(cameraX) > EPSILON ? Math.copySign(1.0D, cameraX) : 1.0D;
            double horizontalDistance = Math.sqrt(cameraX * cameraX + cameraZ * cameraZ);
            projectedY = -cameraY / Math.max(EPSILON, horizontalDistance * tanVertical);
        } else {
            projectedX = cameraX / (cameraZ * tanHorizontal);
            projectedY = -cameraY / (cameraZ * tanVertical);
            if (Math.abs(projectedX) <= 1.0D && Math.abs(projectedY) <= 1.0D) return null;
        }

        double scale = Math.max(Math.abs(projectedX), Math.abs(projectedY));
        if (scale < EPSILON) scale = 1.0D;
        double directionX = projectedX / scale;
        double directionY = projectedY / scale;
        float halfWidth = Math.max(1.0F, width * 0.5F - inset);
        float halfHeight = Math.max(1.0F, height * 0.5F - inset);
        return new Point(
            width * 0.5F + (float) directionX * halfWidth,
            height * 0.5F + (float) directionY * halfHeight,
            (float) directionX,
            (float) directionY,
            behind);
    }

    static final class Point {

        final float x;
        final float y;
        final float directionX;
        final float directionY;
        final boolean behind;

        Point(float x, float y, float directionX, float directionY, boolean behind) {
            this.x = x;
            this.y = y;
            this.directionX = directionX;
            this.directionY = directionY;
            this.behind = behind;
        }
    }
}
