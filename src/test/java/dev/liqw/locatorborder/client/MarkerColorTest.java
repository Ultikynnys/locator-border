package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MarkerColorTest {

    @Test
    public void parsesRgbHexWithOptionalHash() {
        assertEquals(0x12ABEF, MarkerColor.parse("12ABEF"));
        assertEquals(0x55FFFF, MarkerColor.parse("#55ffff"));
    }

    @Test
    public void extractsNormalizedComponents() {
        int color = MarkerColor.parse("804020");

        assertEquals(128.0F / 255.0F, MarkerColor.red(color), 0.0F);
        assertEquals(64.0F / 255.0F, MarkerColor.green(color), 0.0F);
        assertEquals(32.0F / 255.0F, MarkerColor.blue(color), 0.0F);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedValues() {
        MarkerColor.parse("red");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNull() {
        MarkerColor.parse(null);
    }
}
