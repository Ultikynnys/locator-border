package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;

import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class PlayerVisibility {

    private PlayerVisibility() {}

    static boolean shouldShow(Minecraft minecraft, PlayerSnapshotMessage.PlayerPosition player) {
        if (ModConfig.showPlayers == PlayerVisibilityMode.ALL) return true;
        return FtbTeams.sameTeam(minecraft.thePlayer.getUniqueID(), player.id);
    }
}
