package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class LabelDisplayTest {

    @Test
    public void parsesAllModes() {
        assertEquals(LabelDisplay.LOOK_AT, LabelDisplay.parse("LOOK_AT"));
        assertEquals(LabelDisplay.ALWAYS, LabelDisplay.parse("always"));
        assertEquals(LabelDisplay.NONE, LabelDisplay.parse("NONE"));
    }

    @Test
    public void migratesLegacyValues() {
        assertEquals(LabelDisplay.LOOK_AT, LabelDisplay.parse("PLAYERNAME"));
        assertEquals(LabelDisplay.LOOK_AT, LabelDisplay.parse("DISTANCE"));
        assertEquals(LabelDisplay.LOOK_AT, LabelDisplay.parse("true"));
        assertEquals(LabelDisplay.NONE, LabelDisplay.parse("false"));
    }

    @Test
    public void revealsPerMode() {
        assertTrue(LabelDisplay.LOOK_AT.reveals(true));
        assertFalse(LabelDisplay.LOOK_AT.reveals(false));
        assertTrue(LabelDisplay.ALWAYS.reveals(false));
        assertFalse(LabelDisplay.NONE.reveals(true));
    }
}
