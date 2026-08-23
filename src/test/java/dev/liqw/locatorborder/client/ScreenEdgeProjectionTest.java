package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ScreenEdgeProjectionTest {

    // Camera looking along +Z (south). Camera right points along -X.
    private static final double FORWARD_X = 0.0D;
    private static final double FORWARD_Z = 1.0D;
    private static final double RIGHT_X = -1.0D;
    private static final double RIGHT_Z = 0.0D;

    @Test
    public void leavesVisibleTargetsInWorldSpace() {
        assertNull(project(0.0D, 0.0D, 10.0D));
    }

    @Test
    public void clampsToLeftAndRightEdges() {
        // dx > 0 is the camera's left (right axis is -X), dx < 0 is the camera's right.
        ScreenEdgeProjection.Point left = project(20.0D, 0.0D, 10.0D);
        ScreenEdgeProjection.Point right = project(-20.0D, 0.0D, 10.0D);

        assertEquals(10.0F, left.x, 0.01F);
        assertEquals(190.0F, right.x, 0.01F);
    }

    @Test
    public void clampsVerticalAndCornerDirections() {
        ScreenEdgeProjection.Point top = project(0.0D, 20.0D, 10.0D);
        ScreenEdgeProjection.Point corner = project(-20.0D, 30.0D, 10.0D);

        assertEquals(10.0F, top.y, 0.01F);
        assertTrue(corner.x > 100.0F);
        assertEquals(10.0F, corner.y, 0.01F);
    }

    @Test
    public void clampsTargetsBehindCameraToTheirSide() {
        // A target behind the camera has no visible world dot, so it gets a screen
        // edge dot clamped to the side the player must turn toward.
        ScreenEdgeProjection.Point left = project(10.0D, 0.0D, -10.0D);
        ScreenEdgeProjection.Point right = project(-10.0D, 0.0D, -10.0D);

        assertEquals(10.0F, left.x, 0.01F);
        assertEquals(190.0F, right.x, 0.01F);
    }

    @Test
    public void clampsTargetsToSideOfCameraToTheirEdge() {
        // A teammate at the player's 3 o'clock / 9 o'clock has no forward component,
        // so the world dot is not visible and the edge dot clamps to its own side.
        ScreenEdgeProjection.Point left = project(20.0D, 0.0D, 0.0D);
        ScreenEdgeProjection.Point right = project(-20.0D, 0.0D, 0.0D);

        assertEquals(10.0F, left.x, 0.01F);
        assertEquals(190.0F, right.x, 0.01F);
    }

    private static ScreenEdgeProjection.Point project(double dx, double dy, double dz) {
        return ScreenEdgeProjection.project(dx, dy, dz, FORWARD_X, FORWARD_Z, RIGHT_X, RIGHT_Z, 70.0F, 200, 100, 10);
    }
}
