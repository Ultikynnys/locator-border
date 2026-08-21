package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class MarkerGeometryTest {

    @Test
    public void filtersByDimension() {
        assertTrue(MarkerGeometry.isInDimension(2, 2));
        assertFalse(MarkerGeometry.isInDimension(2, 3));
    }

    @Test
    public void calculatesThreeDimensionalDistance() {
        assertEquals(13.0D, MarkerGeometry.distance(3.0D, 4.0D, 12.0D), 0.0D);
    }
}
