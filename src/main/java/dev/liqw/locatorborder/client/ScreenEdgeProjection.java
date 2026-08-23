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
        boolean worldVisible = forwardDot > 0.0D && horizontalAngle <= halfHorizontalAngle
            && Math.abs(verticalAngle) <= halfVerticalAngle;
        if (worldVisible) return null;

        // Off-screen: derive a normalized view-space position and clamp it to the
        // screen edge. Any of the four edges (left/right/top/bottom) can be chosen.
        // Targets behind the camera are reflected into the front half, keeping their
        // left/right side, so a player directly behind is not confused for one on
        // your left or right (it lands near the centre instead).
        double effectiveForward = forwardDot <= 0.0D ? -forwardDot : forwardDot;
        if (effectiveForward < EPSILON) effectiveForward = EPSILON;
        double normalizedX = rightDot / (effectiveForward * tanHorizontal);
        double normalizedY = -Math.tan(verticalAngle) / (effectiveForward * tanVertical);

        double scale = Math.max(Math.abs(normalizedX), Math.abs(normalizedY));
        if (scale < EPSILON) scale = 1.0D;
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
