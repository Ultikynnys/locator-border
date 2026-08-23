package dev.liqw.locatorborder.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import dev.liqw.locatorborder.CommonProxy;
import dev.liqw.locatorborder.LocatorBorder;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.HelloMessage;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class ClientState {

    private static final int HELLO_RETRY_TICKS = 40;
    private static final int RETRY_WARNING_ATTEMPTS = 5;
    private static final HandshakeState HANDSHAKE = new HandshakeState(HELLO_RETRY_TICKS);

    private static volatile Snapshot current = Snapshot.empty();
    private static volatile boolean snapshotReceived;
    private static volatile boolean firstSnapshotLogged;
    private int ticksSinceSnapshot;

    static void receive(PlayerSnapshotMessage message) {
        Snapshot previous = current;
        if (message.getSequence() <= previous.sequence) return;
        current = new Snapshot(message.getSequence(), message.getPlayers());
        snapshotReceived = true;
        boolean firstAcknowledgement = HANDSHAKE.acknowledge();
        if (!firstSnapshotLogged) {
            firstSnapshotLogged = true;
            LocatorBorder.LOG.info(
                "Received first Locator Border snapshot with {} player waypoint(s).",
                message.getPlayers()
                    .size());
        } else if (firstAcknowledgement) {
            LocatorBorder.LOG.info("Locator Border connection confirmed by snapshot delivery.");
        }
    }

    public Snapshot get() {
        return current;
    }

    // The waypoints to render for the given client dimension: the live snapshot,
    // plus an optional test waypoint at the world origin when enabled in config.
    public List<PlayerSnapshotMessage.PlayerPosition> waypoints(int currentDimension) {
        if (!ModConfig.testWaypoint) return current.players;
        List<PlayerSnapshotMessage.PlayerPosition> result = new ArrayList<PlayerSnapshotMessage.PlayerPosition>(
            current.players.size() + 1);
        result.addAll(current.players);
        result.add(TestWaypoint.forDimension(currentDimension));
        return result;
    }

    // The waypoints that should actually be drawn in the given dimension: same
    // dimension as the client and not filtered out by visibility. Shared by the
    // world and screen-edge renderers so both draw the same set.
    public List<PlayerSnapshotMessage.PlayerPosition> waypointsForRender(int currentDimension) {
        List<PlayerSnapshotMessage.PlayerPosition> all = waypoints(currentDimension);
        List<PlayerSnapshotMessage.PlayerPosition> result = new ArrayList<PlayerSnapshotMessage.PlayerPosition>(
            all.size());
        for (PlayerSnapshotMessage.PlayerPosition player : all) {
            if (!MarkerGeometry.isInDimension(player.dimension, currentDimension)) continue;
            if (!PlayerVisibility.shouldShow(player)) continue;
            result.add(player);
        }
        return result;
    }

    @SubscribeEvent
    public void onConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        resetConnection();
    }

    @SubscribeEvent
    public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        resetConnection();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        boolean connectionReady = minecraft.theWorld != null && minecraft.thePlayer != null;
        if (HANDSHAKE.shouldSend(connectionReady)) {
            CommonProxy.NETWORK.sendToServer(new HelloMessage(HelloMessage.PROTOCOL_VERSION));
            int attempts = HANDSHAKE.getAttempts();
            if (attempts == 1) {
                LocatorBorder.LOG.info("Sent Locator Border handshake after the client world became ready.");
            } else if (attempts == RETRY_WARNING_ATTEMPTS || attempts % RETRY_WARNING_ATTEMPTS == 0) {
                LocatorBorder.LOG
                    .warn("Locator Border handshake is still unacknowledged after {} attempts; retrying.", attempts);
            }
        }
        if (!connectionReady) return;
        if (snapshotReceived) {
            snapshotReceived = false;
            ticksSinceSnapshot = 0;
        } else {
            ticksSinceSnapshot++;
        }
        if (ticksSinceSnapshot > ModConfig.staleSnapshotTicks && !current.players.isEmpty()) {
            current = Snapshot.empty();
        }
    }

    private void resetConnection() {
        current = Snapshot.empty();
        snapshotReceived = false;
        firstSnapshotLogged = false;
        ticksSinceSnapshot = 0;
        HANDSHAKE.reset();
    }

    static final class Snapshot {

        final long sequence;
        final List<PlayerSnapshotMessage.PlayerPosition> players;

        Snapshot(long sequence, List<PlayerSnapshotMessage.PlayerPosition> players) {
            this.sequence = sequence;
            this.players = players;
        }

        static Snapshot empty() {
            return new Snapshot(-1L, Collections.<PlayerSnapshotMessage.PlayerPosition>emptyList());
        }
    }
}
