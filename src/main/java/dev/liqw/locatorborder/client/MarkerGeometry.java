package dev.liqw.locatorborder.client;

final class MarkerGeometry {

    // Height above the feet where the marker floats, matching the vanilla
    // nameplate position so the marker never overlaps the player's body.
    static final double MARKER_HEIGHT = 2.2D;

    private MarkerGeometry() {}

    static boolean isInDimension(int markerDimension, int playerDimension) {
        return markerDimension == playerDimension;
    }

    static double distance(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
