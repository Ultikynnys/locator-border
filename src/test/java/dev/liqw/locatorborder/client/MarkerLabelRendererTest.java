package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import dev.liqw.locatorborder.ModConfig;

public final class MarkerLabelRendererTest {

    @Test
    public void hidesLabelWhenNotFocusedAndNothingEnabled() {
        ModConfig.playerNameDisplay = LabelDisplay.LOOK_AT;
        ModConfig.distanceDisplay = LabelDisplay.NONE;

        assertNull(MarkerLabelRenderer.text("Player", 42.9D, 0.0F));
    }

    @Test
    public void includesPlayerNameWhenFocused() {
        ModConfig.playerNameDisplay = LabelDisplay.LOOK_AT;
        ModConfig.distanceDisplay = LabelDisplay.NONE;

        assertEquals("Player", MarkerLabelRenderer.text("Player", 42.9D, 1.0F));
    }

    @Test
    public void includesEnabledDistanceWhenFocused() {
        ModConfig.playerNameDisplay = LabelDisplay.LOOK_AT;
        ModConfig.distanceDisplay = LabelDisplay.LOOK_AT;

        assertEquals("Player 42m", MarkerLabelRenderer.text("Player", 42.9D, 1.0F));
    }

    @Test
    public void showsDistanceOnlyWhenNameDisabled() {
        ModConfig.playerNameDisplay = LabelDisplay.NONE;
        ModConfig.distanceDisplay = LabelDisplay.LOOK_AT;

        assertEquals("42m", MarkerLabelRenderer.text("Player", 42.9D, 1.0F));
    }

    @Test
    public void alwaysShowsNameEvenWhenUnfocused() {
        ModConfig.playerNameDisplay = LabelDisplay.ALWAYS;
        ModConfig.distanceDisplay = LabelDisplay.NONE;

        assertEquals("Player", MarkerLabelRenderer.text("Player", 42.9D, 0.0F));
    }

    @Test
    public void alwaysShowsDistanceEvenWhenUnfocused() {
        ModConfig.playerNameDisplay = LabelDisplay.NONE;
        ModConfig.distanceDisplay = LabelDisplay.ALWAYS;

        assertEquals("42m", MarkerLabelRenderer.text("Player", 42.9D, 0.0F));
    }

    @Test
    public void formatsDistanceConsistently() {
        assertEquals("42m", MarkerLabelRenderer.distance(42.9D));
    }
}
