package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import dev.liqw.locatorborder.ModConfig;

public final class MarkerLabelRendererTest {

    @Test
    public void hidesLabelWhenNotFocused() {
        ModConfig.displayPlayerName = true;
        ModConfig.displayDistance = true;

        assertNull(MarkerLabelRenderer.text("Player", 42.9D, 0.0F));
    }

    @Test
    public void includesPlayerNameWhenFocused() {
        ModConfig.displayPlayerName = true;
        ModConfig.displayDistance = false;

        assertEquals("Player", MarkerLabelRenderer.text("Player", 42.9D, 1.0F));
    }

    @Test
    public void includesEnabledDistanceWhenFocused() {
        ModConfig.displayPlayerName = true;
        ModConfig.displayDistance = true;

        assertEquals("Player 42m", MarkerLabelRenderer.text("Player", 42.9D, 1.0F));
    }

    @Test
    public void showsDistanceOnlyWhenNameDisabled() {
        ModConfig.displayPlayerName = false;
        ModConfig.displayDistance = true;

        assertEquals("42m", MarkerLabelRenderer.text("Player", 42.9D, 1.0F));
    }

    @Test
    public void formatsDistanceConsistently() {
        assertEquals("42m", MarkerLabelRenderer.distance(42.9D));
    }
}
