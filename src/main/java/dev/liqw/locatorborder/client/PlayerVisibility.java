package dev.liqw.locatorborder.client;

import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class PlayerVisibility {

    private PlayerVisibility() {}

    static boolean shouldShow(PlayerSnapshotMessage.PlayerPosition player) {
        if (ModConfig.showPlayers == PlayerVisibilityMode.ALL) return true;
        return player.sameTeam;
    }
}
