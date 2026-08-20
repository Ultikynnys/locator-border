package dev.liqw.locatorborder.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.liqw.locatorborder.CommonProxy;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class PlayerTracker {

    private static final Set<UUID> COMPATIBLE_CLIENTS = Collections
        .newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private int ticks;

    public static void accept(UUID playerId) {
        COMPATIBLE_CLIENTS.add(playerId);
    }

    public static void reject(UUID playerId) {
        COMPATIBLE_CLIENTS.remove(playerId);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        COMPATIBLE_CLIENTS.remove(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !ModConfig.enabled) return;
        if (++ticks < ModConfig.updateIntervalTicks) return;
        ticks = 0;

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) return;

        @SuppressWarnings("unchecked")
        java.util.List<EntityPlayerMP> online = server.getConfigurationManager().playerEntityList;
        long sequence = SEQUENCE.incrementAndGet();

        for (EntityPlayerMP recipient : online) {
            if (!COMPATIBLE_CLIENTS.contains(recipient.getUniqueID())) continue;
            ArrayList<PlayerSnapshotMessage.PlayerPosition> positions = new ArrayList<PlayerSnapshotMessage.PlayerPosition>();
            for (EntityPlayerMP target : online) {
                if (target == recipient) continue;
                if (ModConfig.sameDimensionOnly && target.dimension != recipient.dimension) continue;
                if (positions.size() >= ModConfig.maximumPlayersPerSnapshot) break;
                positions.add(
                    new PlayerSnapshotMessage.PlayerPosition(
                        target.getUniqueID(),
                        target.getCommandSenderName(),
                        target.dimension,
                        target.posX,
                        target.posY,
                        target.posZ));
            }
            CommonProxy.NETWORK.sendTo(new PlayerSnapshotMessage(sequence, positions), recipient);
        }
    }
}
