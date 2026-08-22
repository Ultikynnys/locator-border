package dev.liqw.locatorborder.client;

import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class MarkerColorResolver {

    private MarkerColorResolver() {}

    static int resolve(PlayerSnapshotMessage.PlayerPosition player) {
        if (ModConfig.colorSource == ColorSource.FTB_TEAM) return FtbTeamColor.resolve(player.id);
        return MarkerColor.fromPlayer(player.id);
    }
}
