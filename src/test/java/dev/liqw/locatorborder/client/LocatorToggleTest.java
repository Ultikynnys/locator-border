package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LocatorToggleTest {

    @Test
    public void startsEnabledAndAlternatesOnEachToggle() {
        LocatorToggle toggle = new LocatorToggle();

        assertTrue(toggle.isEnabled());

        toggle.toggle();
        assertFalse(toggle.isEnabled());

        toggle.toggle();
        assertTrue(toggle.isEnabled());
    }
}
