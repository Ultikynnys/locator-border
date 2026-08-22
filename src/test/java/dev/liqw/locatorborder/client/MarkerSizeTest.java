package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dev.liqw.locatorborder.ModConfig;

public final class MarkerSizeTest {

    @Test
    public void selectsUnfocusedScale() {
        ModConfig.unfocusedScale = 0.75F;
        ModConfig.focusedScale = 1.5F;

        assertEquals(0.75F, MarkerSize.scale(0.0F), 0.0F);
    }

    @Test
    public void selectsFocusedScale() {
        ModConfig.unfocusedScale = 0.75F;
        ModConfig.focusedScale = 1.5F;

        assertEquals(1.5F, MarkerSize.scale(1.0F), 0.0F);
    }

    @Test
    public void interpolatesBetweenScales() {
        ModConfig.unfocusedScale = 0.75F;
        ModConfig.focusedScale = 1.5F;

        assertEquals(1.125F, MarkerSize.scale(0.5F), 0.0001F);
    }
}
