package dev.liqw.locatorborder.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class PlayerSnapshotMessageTest {

    @Test
    public void roundTripsTeamColorAndSameTeam() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        PlayerSnapshotMessage.PlayerPosition position = new PlayerSnapshotMessage.PlayerPosition(
            id,
            "Alice",
            0,
            1.5D,
            2.5D,
            3.5D,
            0xB342FF,
            true);
        List<PlayerSnapshotMessage.PlayerPosition> players = new ArrayList<PlayerSnapshotMessage.PlayerPosition>();
        players.add(position);
        PlayerSnapshotMessage original = new PlayerSnapshotMessage(7L, players);

        ByteBuf buffer = Unpooled.buffer();
        original.toBytes(buffer);
        PlayerSnapshotMessage decoded = new PlayerSnapshotMessage();
        decoded.fromBytes(buffer);

        assertTrue(decoded.isValid());
        assertEquals(7L, decoded.getSequence());
        assertEquals(
            1,
            decoded.getPlayers()
                .size());
        PlayerSnapshotMessage.PlayerPosition roundTripped = decoded.getPlayers()
            .get(0);
        assertEquals(id, roundTripped.id);
        assertEquals("Alice", roundTripped.name);
        assertEquals(0, roundTripped.dimension);
        assertEquals(1.5D, roundTripped.x, 0.0D);
        assertEquals(2.5D, roundTripped.y, 0.0D);
        assertEquals(3.5D, roundTripped.z, 0.0D);
        assertEquals(0xB342FF, roundTripped.teamColor);
        assertTrue(roundTripped.sameTeam);
    }

    @Test
    public void roundTripsNoTeamDefaults() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        PlayerSnapshotMessage.PlayerPosition position = new PlayerSnapshotMessage.PlayerPosition(
            id,
            "Bob",
            -1,
            -10.0D,
            64.0D,
            10.0D,
            0xFFFFFF,
            false);
        List<PlayerSnapshotMessage.PlayerPosition> players = new ArrayList<PlayerSnapshotMessage.PlayerPosition>();
        players.add(position);
        PlayerSnapshotMessage original = new PlayerSnapshotMessage(8L, players);

        ByteBuf buffer = Unpooled.buffer();
        original.toBytes(buffer);
        PlayerSnapshotMessage decoded = new PlayerSnapshotMessage();
        decoded.fromBytes(buffer);

        assertTrue(decoded.isValid());
        PlayerSnapshotMessage.PlayerPosition roundTripped = decoded.getPlayers()
            .get(0);
        assertEquals(0xFFFFFF, roundTripped.teamColor);
        assertFalse(roundTripped.sameTeam);
    }
}
