package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MarkerSizeTest {

    @Test
    public void keepsMinimumDistanceScaleInsideThreshold() {
        assertEquals(0.02F, MarkerSize.distanceScale(0.0D), 0.0F);
        assertEquals(0.02F, MarkerSize.distanceScale(10.0D), 0.0F);
    }

    @Test
    public void growsDistanceScaleBeyondThreshold() {
        assertEquals(0.04F, MarkerSize.distanceScale(20.0D), 0.000001F);
        assertEquals(0.2F, MarkerSize.distanceScale(100.0D), 0.000001F);
    }

    @Test
    public void worldHalfSizeComposesFocusAndDistance() {
        // Base 3.5, unfocused scale 1.0, distance scale at 100 blocks: 0.2.
        assertEquals(3.5F * 0.2F, MarkerSize.worldHalfSize(100.0D, 0.0F), 0.0001F);
        // Focused scale 1.2 multiplies the same distance scale.
        assertEquals(3.5F * 1.2F * 0.2F, MarkerSize.worldHalfSize(100.0D, 1.0F), 0.0001F);
    }

    @Test
    public void screenHalfSizeProjectsTheWorldSize() {
        // FOV 70, scaled height 540: K = 540 / (2 * tan(35 deg)) ~ 385.6.
        // World half-size at 100 blocks: 3.5 * 1.0 * 0.2 = 0.7, projected: 0.7 / 100 * K.
        float halfSize = MarkerSize.screenHalfSize(100.0D, 0.0F, 70.0F, 540);
        assertEquals(2.699F, halfSize, 0.01F);
    }
}
