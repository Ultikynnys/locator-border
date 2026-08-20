package dev.liqw.locatorborder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class HandshakeStateTest {

    @Test
    public void waitsForReadyConnectionAndRetriesAtConfiguredInterval() {
        HandshakeState state = new HandshakeState(3);

        assertFalse(state.shouldSend(false));
        assertEquals(0, state.getAttempts());

        assertTrue(state.shouldSend(true));
        assertFalse(state.shouldSend(true));
        assertFalse(state.shouldSend(true));
        assertTrue(state.shouldSend(true));
        assertEquals(2, state.getAttempts());
    }

    @Test
    public void acknowledgementStopsRetriesAndIsIdempotent() {
        HandshakeState state = new HandshakeState(2);

        assertTrue(state.shouldSend(true));
        assertTrue(state.acknowledge());
        assertTrue(state.isAcknowledged());
        assertFalse(state.acknowledge());
        assertFalse(state.shouldSend(true));
        assertFalse(state.shouldSend(true));
        assertEquals(1, state.getAttempts());
    }

    @Test
    public void resetAllowsHandshakeAfterReconnect() {
        HandshakeState state = new HandshakeState(5);

        assertTrue(state.shouldSend(true));
        state.acknowledge();
        state.reset();

        assertFalse(state.isAcknowledged());
        assertEquals(0, state.getAttempts());
        assertTrue(state.shouldSend(true));
    }
}
