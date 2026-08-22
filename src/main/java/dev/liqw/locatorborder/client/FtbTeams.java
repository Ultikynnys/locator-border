package dev.liqw.locatorborder.client;

import java.util.UUID;

import cpw.mods.fml.common.Loader;
import dev.liqw.locatorborder.LocatorBorder;
import serverutils.lib.data.ForgePlayer;
import serverutils.lib.data.ForgeTeam;
import serverutils.lib.data.Universe;

final class FtbTeams {

    private static boolean unavailableReported = false;

    private FtbTeams() {}

    static boolean isAvailable() {
        if (Loader.isModLoaded("serverutilities") && Universe.getNullable() != null) return true;
        reportUnavailable();
        return false;
    }

    static ForgeTeam teamOf(UUID playerId) {
        if (!isAvailable()) return null;
        ForgePlayer forgePlayer = Universe.getNullable()
            .getPlayer(playerId);
        if (forgePlayer == null || !forgePlayer.hasTeam() || forgePlayer.team == null) return null;
        return forgePlayer.team;
    }

    static boolean sameTeam(UUID first, UUID second) {
        ForgeTeam firstTeam = teamOf(first);
        if (firstTeam == null) return false;
        ForgeTeam secondTeam = teamOf(second);
        return secondTeam != null && firstTeam.equalsTeam(secondTeam);
    }

    private static void reportUnavailable() {
        if (unavailableReported) return;
        unavailableReported = true;
        LocatorBorder.LOG
            .error("ServerUtilities is not installed or not loaded yet; FTB team features are unavailable.");
    }
}
