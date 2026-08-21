package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MarkerSizeTest {

    @Test
    public void selectsUnfocusedScale() {
        assertEquals(0.75F, MarkerSize.scale(false, 0.75F, 1.5F), 0.0F);
    }

    @Test
    public void selectsFocusedScale() {
        assertEquals(1.5F, MarkerSize.scale(true, 0.75F, 1.5F), 0.0F);
    }
}
