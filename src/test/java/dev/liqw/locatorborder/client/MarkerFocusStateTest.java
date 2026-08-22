package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Test;

public final class MarkerFocusStateTest {

    @After
    public void clearState() {
        MarkerFocusState.clear();
    }

    @Test
    public void switchesImmediatelyWhenFocused() {
        UUID id = new UUID(1L, 2L);

        assertEquals(1.0F, MarkerFocusState.updateWorld(id, true), 0.0F);
    }

    @Test
    public void switchesImmediatelyWhenUnfocused() {
        UUID id = new UUID(3L, 4L);

        assertEquals(0.0F, MarkerFocusState.updateWorld(id, false), 0.0F);
    }
}
