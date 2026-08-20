package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MarkerColorTest {

    @Test
    public void parsesRgbHexWithOptionalHash() {
        assertEquals(0x12ABEF, MarkerColor.parse("12ABEF"));
        assertEquals(0x55FFFF, MarkerColor.parse("#55ffff"));
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
