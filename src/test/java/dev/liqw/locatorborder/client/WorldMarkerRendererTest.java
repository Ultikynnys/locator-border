package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class WorldMarkerRendererTest {

    @Test
    public void keepsMinimumScaleInsideThreshold() {
        assertEquals(0.02F, WorldMarkerRenderer.markerScale(0.0D), 0.0F);
        assertEquals(0.02F, WorldMarkerRenderer.markerScale(10.0D), 0.0F);
    }

    @Test
    public void growsScaleBeyondThreshold() {
        assertEquals(0.04F, WorldMarkerRenderer.markerScale(20.0D), 0.000001F);
        assertEquals(0.2F, WorldMarkerRenderer.markerScale(100.0D), 0.000001F);
    }
}
