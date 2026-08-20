package dev.liqw.locatorborder.client;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import dev.liqw.locatorborder.CommonProxy;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.HelloMessage;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class ClientState {

    private static volatile Snapshot current = Snapshot.empty();
    private static volatile boolean snapshotReceived;
    private int ticksSinceSnapshot;

    static void receive(PlayerSnapshotMessage message) {
        Snapshot previous = current;
        if (message.getSequence() <= previous.sequence) return;
        current = new Snapshot(message.getSequence(), message.getPlayers());
        snapshotReceived = true;
    }

    public Snapshot get() {
        return current;
    }

    @SubscribeEvent
    public void onConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        current = Snapshot.empty();
        snapshotReceived = false;
        ticksSinceSnapshot = 0;
        CommonProxy.NETWORK.sendToServer(new HelloMessage(HelloMessage.PROTOCOL_VERSION));
    }

    @SubscribeEvent
    public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        current = Snapshot.empty();
        snapshotReceived = false;
        ticksSinceSnapshot = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().theWorld == null) return;
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
