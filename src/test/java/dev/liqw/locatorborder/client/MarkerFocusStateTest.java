package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Test;

public final class MarkerFocusStateTest {

    @After
    public void clearState() {
        MarkerFocusState.clear();
    }

    @Test
    public void switchesImmediatelyWhenAnimationsAreDisabled() {
        UUID id = new UUID(1L, 2L);

        assertEquals(1.0F, MarkerFocusState.updateWorld(id, true, false, 1.0F), 0.0F);
        assertEquals(0.0F, MarkerFocusState.updateWorld(id, false, false, 1.0F), 0.0F);
    }

    @Test
    public void easesAnimatedFocusProgress() {
        UUID id = new UUID(3L, 4L);

        float first = MarkerFocusState.updateWorld(id, true, true, 1.0F);
        float second = MarkerFocusState.updateWorld(id, true, true, 1.0F);

        assertTrue(first > 0.0F);
        assertTrue(second > first);
        assertTrue(second < 1.0F);
    }
}
