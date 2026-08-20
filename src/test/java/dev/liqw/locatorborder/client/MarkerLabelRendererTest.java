package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MarkerLabelRendererTest {

    @Test
    public void alwaysIncludesPlayerName() {
        assertEquals("Player", MarkerLabelRenderer.text("Player", "42m", false, true));
        assertEquals("Player", MarkerLabelRenderer.text("Player", "42m", true, false));
    }

    @Test
    public void includesEnabledDistanceWhenRevealed() {
        assertEquals("Player 42m", MarkerLabelRenderer.text("Player", "42m", true, true));
    }
}
