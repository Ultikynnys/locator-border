package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PlayerVisibilityModeTest {

    @Test
    public void parsesAllNames() {
        assertEquals(PlayerVisibilityMode.ALL, PlayerVisibilityMode.parse("ALL"));
        assertEquals(PlayerVisibilityMode.SAME_TEAM, PlayerVisibilityMode.parse("same_team"));
    }
}
