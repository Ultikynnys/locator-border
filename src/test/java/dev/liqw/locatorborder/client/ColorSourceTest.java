package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ColorSourceTest {

    @Test
    public void parsesAllNames() {
        assertEquals(ColorSource.UUID, ColorSource.parse("UUID"));
        assertEquals(ColorSource.FTB_TEAM, ColorSource.parse("ftb_team"));
    }

    @Test
    public void migratesLegacyValues() {
        assertEquals(ColorSource.UUID, ColorSource.parse("WAYPOINT"));
        assertEquals(ColorSource.FTB_TEAM, ColorSource.parse("TEAM"));
    }
}
