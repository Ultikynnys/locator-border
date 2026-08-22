package dev.liqw.locatorborder.server;

import java.util.UUID;

import cpw.mods.fml.common.Loader;
import dev.liqw.locatorborder.LocatorBorder;
import serverutils.lib.data.ForgePlayer;
import serverutils.lib.data.ForgeTeam;
import serverutils.lib.data.Universe;

final class FtbTeamResolver {

    static final int NO_TEAM_COLOR = 0xFFFFFF;
    private static boolean unavailableReported = false;

    private FtbTeamResolver() {}

    static int teamColorOf(UUID playerId) {
        ForgeTeam team = teamOf(playerId);
        if (team == null) return NO_TEAM_COLOR;
        return team.getColor()
            .getColor()
            .rgb();
    }

    static boolean sameTeamAs(UUID viewerId, UUID playerId) {
        ForgeTeam viewerTeam = teamOf(viewerId);
        if (viewerTeam == null) return true;
        ForgeTeam otherTeam = teamOf(playerId);
        return otherTeam != null && viewerTeam.equalsTeam(otherTeam);
    }

    static ForgeTeam teamOf(UUID playerId) {
        if (!isAvailable()) return null;
        ForgePlayer forgePlayer = Universe.getNullable()
            .getPlayer(playerId);
        if (forgePlayer == null || !forgePlayer.hasTeam() || forgePlayer.team == null) return null;
        return forgePlayer.team;
    }

    private static boolean isAvailable() {
        if (Loader.isModLoaded("serverutilities") && Universe.getNullable() != null) return true;
        reportUnavailable();
        return false;
    }

    private static void reportUnavailable() {
        if (unavailableReported) return;
        unavailableReported = true;
        LocatorBorder.LOG.error(
            "ServerUtilities is not installed or not loaded; FTB team colors and team visibility are unavailable.");
    }
}
