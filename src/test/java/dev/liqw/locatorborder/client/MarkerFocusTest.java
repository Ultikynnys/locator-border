package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class MarkerFocusTest {

    @Test
    public void focusesMarkerUnderCrosshair() {
        double alignment = MarkerFocus.alignment(0.0D, 0.0D, 1.0D, 0.0D, 0.0D, 100.0D);

        assertTrue(MarkerFocus.isFocused(alignment, MarkerFocus.AIM_ALIGNMENT));
    }

    @Test
    public void rejectsMarkerBehindCamera() {
        double alignment = MarkerFocus.alignment(0.0D, 0.0D, 1.0D, 0.0D, 0.0D, -100.0D);

        assertFalse(MarkerFocus.isFocused(alignment, MarkerFocus.AIM_ALIGNMENT));
    }

    @Test
    public void acceptsMarkerInsidePracticalAimCone() {
        double alignment = MarkerFocus.alignment(0.0D, 0.0D, 1.0D, 5.0D, 0.0D, 100.0D);

        assertTrue(MarkerFocus.isFocused(alignment, MarkerFocus.AIM_ALIGNMENT));
    }

    @Test
    public void rejectsMarkerOutsideAimCone() {
        double alignment = MarkerFocus.alignment(0.0D, 0.0D, 1.0D, 20.0D, 0.0D, 100.0D);

        assertFalse(MarkerFocus.isFocused(alignment, MarkerFocus.AIM_ALIGNMENT));
    }

    @Test
    public void supportsAllFocusModes() {
        assertTrue(MarkerFocus.reveal(FocusTrigger.ALWAYS, false, false));
        assertTrue(MarkerFocus.reveal(FocusTrigger.HOVER, true, false));
        assertFalse(MarkerFocus.reveal(FocusTrigger.HOVER, false, false));
        assertTrue(MarkerFocus.reveal(FocusTrigger.FOCAL, true, false));
        assertTrue(MarkerFocus.reveal(FocusTrigger.PLAYER_LIST, false, true));
        assertFalse(MarkerFocus.reveal(FocusTrigger.PLAYER_LIST, true, false));
        assertFalse(MarkerFocus.reveal(FocusTrigger.NONE, true, true));
    }

    @Test
    public void prefersBetterAlignmentThenNearerMarker() {
        assertTrue(MarkerFocus.isBetter(0.999D, 10000.0D, 0.998D, 10.0D));
        assertTrue(MarkerFocus.isBetter(0.999D, 100.0D, 0.999D, 200.0D));
        assertFalse(MarkerFocus.isBetter(0.998D, 10.0D, 0.999D, 10000.0D));
    }
}
