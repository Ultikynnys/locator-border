package dev.liqw.locatorborder.client;

final class ScreenEdgeProjection {

    private static final double EPSILON = 0.0001D;

    private ScreenEdgeProjection() {}

    static Point project(double dx, double dy, double dz, double forwardX, double forwardZ, double rightX,
        double rightZ, float verticalFovDegrees, int width, int height, int inset) {
        // Horizontal line from the camera to the target; the vertical component is
        // removed so the left/right and front/back decision stays in the horizontal
        // plane, matching how a screen-edge indicator reads on screen.
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < EPSILON) return null;
        double lineX = dx / horizontalDistance;
        double lineZ = dz / horizontalDistance;

        // Horizontal camera axes, vertical component removed and normalized.
        double forwardLength = Math.sqrt(forwardX * forwardX + forwardZ * forwardZ);
        double rightLength = Math.sqrt(rightX * rightX + rightZ * rightZ);
        if (forwardLength < EPSILON || rightLength < EPSILON) return null;
        double forwardXh = forwardX / forwardLength;
        double forwardZh = forwardZ / forwardLength;
        double rightXh = rightX / rightLength;
        double rightZh = rightZ / rightLength;

        // Dot products against the view line: forwardDot is cos of the horizontal
        // angle (positive when the target is ahead), rightDot is the side (positive
        // when the target is to the camera's right).
        double forwardDot = forwardXh * lineX + forwardZh * lineZ;
        double rightDot = rightXh * lineX + rightZh * lineZ;

        double tanVertical = Math.tan(Math.toRadians(verticalFovDegrees) * 0.5D);
        double tanHorizontal = tanVertical * width / Math.max(1.0D, height);
        double halfHorizontalAngle = Math.atan(tanHorizontal);
        double halfVerticalAngle = Math.toRadians(verticalFovDegrees) * 0.5D;
        double horizontalAngle = Math.acos(clamp(forwardDot, -1.0D, 1.0D));
        double verticalAngle = Math.atan2(dy, horizontalDistance);

        // When the target is in front and inside both frustum halves the world-space
        // dot is visible, so the world dot wins and no edge dot is drawn.
        boolean worldVisible = forwardDot > 0.0D
            && horizontalAngle <= halfHorizontalAngle
            && Math.abs(verticalAngle) <= halfVerticalAngle;
        if (worldVisible) return null;

        // Off-screen: derive a normalized view-space position and clamp it to the
        // screen edge. Behind the camera there is no projection, so clamp straight
        // to the side the player must turn toward.
        double normalizedX;
        double normalizedY;
        if (forwardDot <= 0.0D) {
            normalizedX = Math.copySign(1.0D, rightDot);
            normalizedY = -Math.tan(verticalAngle);
        } else {
            normalizedX = rightDot / (forwardDot * tanHorizontal);
            normalizedY = -Math.tan(verticalAngle) / (forwardDot * tanVertical);
        }

        double scale = Math.max(1.0D, Math.max(Math.abs(normalizedX), Math.abs(normalizedY)));
        double directionX = normalizedX / scale;
        double directionY = normalizedY / scale;
        float halfWidth = Math.max(1.0F, width * 0.5F - inset);
        float halfHeight = Math.max(1.0F, height * 0.5F - inset);
        return new Point(
            width * 0.5F + (float) directionX * halfWidth,
            height * 0.5F + (float) directionY * halfHeight,
            (float) directionX,
            (float) directionY);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    static final class Point {

        final float x;
        final float y;
        final float directionX;
        final float directionY;

        Point(float x, float y, float directionX, float directionY) {
            this.x = x;
            this.y = y;
            this.directionX = directionX;
            this.directionY = directionY;
        }
    }
}
