package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MarkerColorTest {

    @Test
    public void assignsStablePlayerColors() {
        java.util.UUID first = new java.util.UUID(1L, 2L);
        java.util.UUID second = new java.util.UUID(3L, 4L);

        assertEquals(MarkerColor.fromPlayer(first), MarkerColor.fromPlayer(first));
        org.junit.Assert.assertNotEquals(MarkerColor.fromPlayer(first), MarkerColor.fromPlayer(second));
    }

    @Test
    public void extractsNormalizedComponents() {
        int color = 0x804020;

        assertEquals(128.0F / 255.0F, MarkerColor.red(color), 0.0F);
        assertEquals(64.0F / 255.0F, MarkerColor.green(color), 0.0F);
        assertEquals(32.0F / 255.0F, MarkerColor.blue(color), 0.0F);
    }

    @Test
    public void neverReturnsBlackForFill() {
        assertEquals(0x808080, MarkerColor.ensureVisible(0x000000));
        org.junit.Assert.assertNotEquals(0x000000, MarkerColor.ensureVisible(0x000000));
    }

    @Test
    public void liftsNearBlackToVisibleBrightness() {
        int lifted = MarkerColor.ensureVisible(0x000010);

        assertEquals(0, lifted >> 16 & 255);
        assertEquals(0, lifted >> 8 & 255);
        assertEquals(64, lifted & 255);
    }

    @Test
    public void keepsBrightColorsUnchanged() {
        int color = 0x804020;

        assertEquals(color, MarkerColor.ensureVisible(color));
        assertEquals(0xFFFFFF, MarkerColor.ensureVisible(0xFFFFFF));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullPlayerId() {
        MarkerColor.fromPlayer(null);
    }
}
