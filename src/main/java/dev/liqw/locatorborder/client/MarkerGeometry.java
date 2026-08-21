package dev.liqw.locatorborder.client;

final class MarkerGeometry {

    static final double HEIGHT = 1.0D;

    private MarkerGeometry() {}

    static boolean isInDimension(int markerDimension, int playerDimension) {
        return markerDimension == playerDimension;
    }

    static double distance(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
