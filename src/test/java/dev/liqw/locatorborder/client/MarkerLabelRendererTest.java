package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class MarkerLabelRendererTest {

    @Test
    public void hidesLabelWhenNotRevealed() {
        assertNull(MarkerLabelRenderer.text("Player", 42.9D, false, true, false));
        assertNull(MarkerLabelRenderer.text("Player", 42.9D, false, true, true));
    }

    @Test
    public void includesPlayerNameWhenRevealed() {
        assertEquals("Player", MarkerLabelRenderer.text("Player", 42.9D, true, true, false));
    }

    @Test
    public void includesEnabledDistanceWhenRevealed() {
        assertEquals("Player 42m", MarkerLabelRenderer.text("Player", 42.9D, true, true, true));
    }

    @Test
    public void formatsDistanceConsistently() {
        assertEquals("42m", MarkerLabelRenderer.distance(42.9D));
    }
}
