package dev.liqw.locatorborder.client;

import java.util.UUID;

import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class TestWaypoint {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String NAME = "TEST";
    private static final int COLOR = 0xFF55FF;

    private TestWaypoint() {}

    // A synthetic waypoint at the world origin, in the given dimension so it passes
    // the renderers' same-dimension check. Used to test rendering without players.
    static PlayerSnapshotMessage.PlayerPosition forDimension(int dimension) {
        return new PlayerSnapshotMessage.PlayerPosition(ID, NAME, dimension, 0.0D, 0.0D, 0.0D, COLOR, true);
    }
}
