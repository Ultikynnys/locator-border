package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ScreenEdgeProjectionTest {

    @Test
    public void leavesVisibleTargetsInWorldSpace() {
        assertNull(ScreenEdgeProjection.project(0.0D, 0.0D, 10.0D, 70.0F, 200, 100, 10));
    }

    @Test
    public void clampsToLeftAndRightEdges() {
        ScreenEdgeProjection.Point left = ScreenEdgeProjection.project(-20.0D, 0.0D, 10.0D, 70.0F, 200, 100, 10);
        ScreenEdgeProjection.Point right = ScreenEdgeProjection.project(20.0D, 0.0D, 10.0D, 70.0F, 200, 100, 10);

        assertEquals(10.0F, left.x, 0.01F);
        assertEquals(190.0F, right.x, 0.01F);
    }

    @Test
    public void clampsVerticalAndCornerDirections() {
        ScreenEdgeProjection.Point top = ScreenEdgeProjection.project(0.0D, 20.0D, 10.0D, 70.0F, 200, 100, 10);
        ScreenEdgeProjection.Point corner = ScreenEdgeProjection.project(30.0D, 30.0D, 10.0D, 70.0F, 200, 100, 10);

        assertEquals(10.0F, top.y, 0.01F);
        assertTrue(corner.x > 100.0F);
        assertEquals(10.0F, corner.y, 0.01F);
    }

    @Test
    public void mapsTargetsBehindCameraToTurningEdge() {
        ScreenEdgeProjection.Point left = ScreenEdgeProjection.project(-1.0D, 0.0D, -10.0D, 70.0F, 200, 100, 10);
        ScreenEdgeProjection.Point right = ScreenEdgeProjection.project(1.0D, 0.0D, -10.0D, 70.0F, 200, 100, 10);

        assertTrue(left.behind);
        assertEquals(10.0F, left.x, 0.01F);
        assertEquals(190.0F, right.x, 0.01F);
    }
}
