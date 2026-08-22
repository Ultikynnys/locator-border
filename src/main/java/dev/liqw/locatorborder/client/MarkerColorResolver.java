package dev.liqw.locatorborder.client;

import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class MarkerColorResolver {

    private MarkerColorResolver() {}

    static int resolve(PlayerSnapshotMessage.PlayerPosition player) {
        int color;
        if (ModConfig.colorSource == ColorSource.FTB_TEAM) {
            color = player.teamColor;
        } else {
            color = MarkerColor.fromPlayer(player.id);
        }
        // The marker outline is black, so the fill must never be (near) black or it disappears.
        return MarkerColor.ensureVisible(color);
    }
}
