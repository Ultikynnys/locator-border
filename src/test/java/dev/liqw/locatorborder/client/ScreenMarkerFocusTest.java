package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ScreenMarkerFocusTest {

    @Test
    public void detectsMouseInsideMarkerBounds() {
        ScreenEdgeProjection.Point point = new ScreenEdgeProjection.Point(50.0F, 40.0F, 1.0F, 0.0F, false);

        assertTrue(ScreenMarkerFocus.hover(50.0F, 40.0F, point, 8.0F, 8.0F));
        assertFalse(ScreenMarkerFocus.hover(60.0F, 40.0F, point, 8.0F, 8.0F));
    }

}
