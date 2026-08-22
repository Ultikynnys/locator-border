package dev.liqw.locatorborder.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;

import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class PlayerVisibility {

    private PlayerVisibility() {}

    static boolean shouldShow(Minecraft minecraft, PlayerSnapshotMessage.PlayerPosition player) {
        if (ModConfig.showPlayers == PlayerVisibilityMode.ALL) return true;
        UUID viewerId = minecraft.thePlayer.getUniqueID();
        // A viewer without a team cannot match any team, so they see everyone.
        return FtbTeams.teamOf(viewerId) == null || FtbTeams.sameTeam(viewerId, player.id);
    }
}
