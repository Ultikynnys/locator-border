package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;

import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class MarkerColorResolver {

    private static final int[] CHAT_COLORS = { 0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00,
        0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF };

    private MarkerColorResolver() {}

    static int resolve(Minecraft minecraft, PlayerSnapshotMessage.PlayerPosition player) {
        if ("WAYPOINT".equals(ModConfig.colorSource)) return MarkerColor.fromPlayer(player.id);
        if ("TEAM".equals(ModConfig.colorSource)) return teamColor(minecraft, player.name);
        throw new IllegalStateException("Unhandled color source: " + ModConfig.colorSource);
    }

    static int teamColor(Minecraft minecraft, String playerName) {
        if (minecraft.theWorld == null) return 0xFFFFFF;
        ScorePlayerTeam team = minecraft.theWorld.getScoreboard()
            .getPlayersTeam(playerName);
        if (team == null) return 0xFFFFFF;
        String prefix = team.getColorPrefix();
        if (prefix == null) return 0xFFFFFF;
        for (int i = prefix.length() - 2; i >= 0; i--) {
            if (prefix.charAt(i) != '\u00a7') continue;
            int index = Character.digit(prefix.charAt(i + 1), 16);
            if (index >= 0) return CHAT_COLORS[index];
        }
        return 0xFFFFFF;
    }
}
