package dev.liqw.locatorborder.client;

import java.util.UUID;

import serverutils.lib.data.ForgeTeam;

final class FtbTeamColor {

    private static final int NO_TEAM_COLOR = 0xFFFFFF;

    private FtbTeamColor() {}

    static int resolve(UUID playerId) {
        ForgeTeam team = FtbTeams.teamOf(playerId);
        if (team == null) return NO_TEAM_COLOR;
        return team.getColor()
            .getColor()
            .rgb();
    }
}
