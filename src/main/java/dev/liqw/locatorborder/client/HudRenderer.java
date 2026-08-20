package dev.liqw.locatorborder.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

final class HudRenderer extends Gui {

    private static final int[] CHAT_COLORS = { 0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00,
        0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF };

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final ClientState state;

    HudRenderer(ClientState state) {
        this.state = state;
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !ModConfig.enabled
            || minecraft.thePlayer == null
            || minecraft.gameSettings.hideGUI) return;

        ScaledResolution resolution = event.resolution;
        ScreenProjection projection = new ScreenProjection(
            minecraft,
            resolution.getScaledWidth(),
            resolution.getScaledHeight());
        setupGl();
        for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
            if (player.dimension != minecraft.thePlayer.dimension) continue;
            renderPlayer(player, projection, event.partialTicks);
        }
        if (ModConfig.compass) renderCompass(projection);
        restoreGl();
    }

    private void renderPlayer(PlayerSnapshotMessage.PlayerPosition target, ScreenProjection projection,
        float partialTicks) {
        double dx = target.x - minecraft.thePlayer.posX;
        double dy = target.y - minecraft.thePlayer.posY;
        double dz = target.z - minecraft.thePlayer.posZ;
        float distance = MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
        float angle = MathHelper
            .wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(-dx, dz)) - minecraft.thePlayer.rotationYaw);
        int baseSize = ModConfig.playerFaces ? faceSize(distance) : 9;
        ScreenProjection.State renderState = projection.compute(target.id, angle, baseSize, baseSize, partialTicks);
        if (renderState.alpha <= 0.0F) return;

        float scale = 1.0F + (ModConfig.focusScale - 1.0F) * renderState.focus;
        int size = Math.max(1, (int) (baseSize * scale));
        GL11.glPushMatrix();
        GL11.glTranslatef(renderState.x, renderState.y, 0.0F);
        if (ModConfig.playerFaces && renderFace(target.id, size, renderState)) {
            // Face rendered from the tab-list skin cache.
        } else {
            int color = waypointColor(target);
            drawRect(-size / 2, -size / 2, -size / 2 + size, -size / 2 + size, renderState.alpha(color));
        }
        if (renderState.focus > 0.0F) renderLabels(target.name, distance, size, renderState);
        if (ModConfig.directionArrows && Math.abs(dy) > 2.0D) renderArrow(dy > 0.0D, size, renderState);
        GL11.glPopMatrix();
    }

    private boolean renderFace(UUID id, int size, ScreenProjection.State state) {
        EntityPlayer loadedPlayer = minecraft.theWorld.func_152378_a(id);
        int outline = outlineColor(id);
        net.minecraft.util.ResourceLocation skin;
        if (loadedPlayer instanceof AbstractClientPlayer) {
            skin = ((AbstractClientPlayer) loadedPlayer).getLocationSkin();
        } else {
            String name = playerName(id);
            if (name == null) return false;
            skin = AbstractClientPlayer.getLocationSkin(name);
            AbstractClientPlayer.getDownloadImageSkin(skin, name);
        }
        int outlineSize = size + 2;
        if ("BORDER".equals(ModConfig.outlineStyle)) {
            drawRect(
                -outlineSize / 2,
                -size / 2,
                -outlineSize / 2 + outlineSize,
                -size / 2 + size,
                state.alpha(outline));
            drawRect(
                -size / 2,
                -outlineSize / 2,
                -size / 2 + size,
                -outlineSize / 2 + outlineSize,
                state.alpha(outline));
        }
        minecraft.getTextureManager()
            .bindTexture(skin);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, state.alpha);
        if ("SHADOW".equals(ModConfig.outlineStyle)) {
            GL11.glColor4f(0.25F, 0.25F, 0.25F, state.alpha);
            Gui.func_152125_a(-size / 2 + 1, -size / 2 + 1, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, state.alpha);
        Gui.func_152125_a(-size / 2, -size / 2, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
        Gui.func_152125_a(-size / 2, -size / 2, 40.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
        return true;
    }

    private void renderLabels(String name, float distance, int iconSize, ScreenProjection.State state) {
        String nameText = ModConfig.displayPlayerName ? name : null;
        String distanceText = ModConfig.displayDistance ? (int) distance + "m" : null;
        if (nameText == null && distanceText == null) return;

        int lineHeight = minecraft.fontRenderer.FONT_HEIGHT;
        int lines = (nameText != null ? 1 : 0) + (distanceText != null ? 1 : 0);
        int blockHeight = lines * lineHeight + (lines - 1) * 2;
        int nameWidth = nameText == null ? 0 : minecraft.fontRenderer.getStringWidth(nameText);
        int distanceWidth = distanceText == null ? 0 : minecraft.fontRenderer.getStringWidth(distanceText);
        int maxWidth = Math.max(nameWidth, distanceWidth);
        int anchorX;
        int anchorY;
        if (Math.abs(state.directionY) > Math.abs(state.directionX)) {
            int centered = -maxWidth / 2;
            if (state.x + centered < 0 || state.x + centered + maxWidth > state.centerX * 2.0F) {
                anchorX = state.x + centered < 0 ? iconSize / 2 + 6 : -iconSize / 2 - maxWidth - 6;
                anchorY = -blockHeight / 2;
            } else {
                anchorX = centered;
                anchorY = state.directionY > 0 ? -iconSize / 2 - blockHeight - 4 : iconSize / 2 + 4;
            }
        } else {
            anchorX = state.directionX > 0 ? -iconSize / 2 - maxWidth - 6 : iconSize / 2 + 6;
            anchorY = -blockHeight / 2;
        }
        int y = anchorY;
        if (nameText != null) {
            minecraft.fontRenderer
                .drawString(nameText, anchorX + (maxWidth - nameWidth) / 2, y, state.alpha(0xFFFFFFFF, state.focus));
            y += lineHeight + 2;
        }
        if (distanceText != null) {
            minecraft.fontRenderer.drawString(
                distanceText,
                anchorX + (maxWidth - distanceWidth) / 2,
                y,
                state.alpha(0xFFAAAAAA, state.focus));
        }
    }

    private void renderArrow(boolean up, int size, ScreenProjection.State state) {
        int color = state.alpha(0xFFFFFFFF);
        int y = up ? -size / 2 - 6 : size / 2 + 2;
        drawRect(-1, y, 2, y + 3, color);
        drawRect(-3, up ? y + 2 : y, 4, up ? y + 4 : y + 2, color);
    }

    private void renderCompass(ScreenProjection projection) {
        String[] labels = { "S", "SE", "W", "SW", "N", "NW", "E", "NE" };
        for (int i = 0; i < labels.length; i++) {
            if ((i & 1) == 1 && !ModConfig.intercardinalCompass) continue;
            float angle = i * 45.0F - minecraft.thePlayer.rotationYaw;
            ScreenProjection.State compassState = projection.computeStatic(angle);
            if (compassState.alpha <= 0.0F) continue;
            int color = "N".equals(labels[i]) ? 0xFFFF5555 : ((i & 1) == 1 ? 0xFFAAAAAA : 0xFFFFFFFF);
            int x = (int) compassState.x - minecraft.fontRenderer.getStringWidth(labels[i]) / 2;
            int y = (int) compassState.y - minecraft.fontRenderer.FONT_HEIGHT / 2;
            minecraft.fontRenderer.drawString(labels[i], x, y, compassState.alpha(color));
        }
    }

    private int waypointColor(PlayerSnapshotMessage.PlayerPosition target) {
        if ("TEAM".equals(ModConfig.colorSource)) return teamColor(target.name);
        int hash = target.id.hashCode();
        int red = Math.min(255, (int) (((hash >> 16) & 255) * 1.8F));
        int green = Math.min(255, (int) (((hash >> 8) & 255) * 1.8F));
        int blue = Math.min(255, (int) ((hash & 255) * 1.8F));
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private int outlineColor(UUID id) {
        String name = playerName(id);
        if ("TEAM".equals(ModConfig.outlineColor)) return name == null ? 0xFFFFFFFF : teamColor(name);
        if ("WAYPOINT".equals(ModConfig.outlineColor)) {
            return waypointColor(new PlayerSnapshotMessage.PlayerPosition(id, name == null ? "" : name, 0, 0, 0, 0));
        }
        return 0xFF000000;
    }

    private String playerName(UUID id) {
        for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
            if (player.id.equals(id)) return player.name;
        }
        return null;
    }

    private int teamColor(String playerName) {
        ScorePlayerTeam team = minecraft.theWorld.getScoreboard()
            .getPlayersTeam(playerName);
        if (team == null) return 0xFFFFFFFF;
        String prefix = team.getColorPrefix();
        if (prefix == null) return 0xFFFFFFFF;
        for (int i = prefix.length() - 2; i >= 0; i--) {
            if (prefix.charAt(i) != '\u00a7') continue;
            int index = Character.digit(prefix.charAt(i + 1), 16);
            if (index >= 0) return 0xFF000000 | CHAT_COLORS[index];
        }
        return 0xFFFFFFFF;
    }

    private int faceSize(float distance) {
        if (ModConfig.distanceScale) {
            if (distance >= 96.0F) return 4;
            if (distance >= 32.0F) return 6;
        }
        return 8;
    }

    private static void setupGl() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    private static void restoreGl() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }
}
