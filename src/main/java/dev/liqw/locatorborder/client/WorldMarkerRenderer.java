package dev.liqw.locatorborder.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class WorldMarkerRenderer {

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final ClientState state;
    private final LocatorToggle locatorToggle;

    WorldMarkerRenderer(ClientState state, LocatorToggle locatorToggle) {
        this.state = state;
        this.locatorToggle = locatorToggle;
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if (!locatorToggle.isEnabled() || minecraft.thePlayer == null
            || minecraft.theWorld == null
            || minecraft.gameSettings.hideGUI) return;

        captureRenderFov();
        PlayerSnapshotMessage.PlayerPosition aimed = aimedMarker(event.partialTicks);
        setupGl();
        try {
            for (PlayerSnapshotMessage.PlayerPosition player : state.waypoints(minecraft.thePlayer.dimension)) {
                if (!MarkerGeometry.isInDimension(player.dimension, minecraft.thePlayer.dimension)) continue;
                if (!PlayerVisibility.shouldShow(player)) continue;
                boolean focused = MarkerFocus.reveal(ModConfig.focusTrigger, player == aimed);
                float focusProgress = MarkerFocusState.updateWorld(player.id, focused);
                renderMarker(player, focusProgress);
            }
        } finally {
            restoreGl();
        }
    }

    private PlayerSnapshotMessage.PlayerPosition aimedMarker(float partialTicks) {
        Vec3 eye = minecraft.renderViewEntity.getPosition(partialTicks);
        eye.yCoord += minecraft.renderViewEntity.getEyeHeight();
        Vec3 look = minecraft.renderViewEntity.getLook(partialTicks);
        PlayerSnapshotMessage.PlayerPosition best = null;
        double bestAlignment = MarkerFocus.AIM_ALIGNMENT;
        double bestDistanceSquared = Double.POSITIVE_INFINITY;
        for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
            if (!MarkerGeometry.isInDimension(player.dimension, minecraft.thePlayer.dimension)) continue;
            double dx = player.x - eye.xCoord;
            double dy = player.y + MarkerGeometry.MARKER_HEIGHT - eye.yCoord;
            double dz = player.z - eye.zCoord;
            double alignment = MarkerFocus.alignment(look.xCoord, look.yCoord, look.zCoord, dx, dy, dz);
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            if (MarkerFocus.isFocused(alignment, MarkerFocus.AIM_ALIGNMENT)
                && MarkerFocus.isBetter(alignment, distanceSquared, bestAlignment, bestDistanceSquared)) {
                best = player;
                bestAlignment = alignment;
                bestDistanceSquared = distanceSquared;
            }
        }
        return best;
    }

    private void renderMarker(PlayerSnapshotMessage.PlayerPosition target, float focusProgress) {
        double dx = target.x - minecraft.thePlayer.posX;
        double dy = target.y - minecraft.thePlayer.posY;
        double dz = target.z - minecraft.thePlayer.posZ;
        double distance = MarkerGeometry.distance(dx, dy, dz);
        int color = MarkerColorResolver.resolve(target);

        GL11.glPushMatrix();
        try {
            GL11.glTranslated(
                target.x - RenderManager.instance.viewerPosX,
                target.y + MarkerGeometry.MARKER_HEIGHT - RenderManager.instance.viewerPosY,
                target.z - RenderManager.instance.viewerPosZ);
            faceCamera();
            float radius = MarkerSize.worldHalfSize(distance, focusProgress) * MarkerSize.worldRenderScale();
            if (!ModConfig.playerFaces || !renderFace(target, radius)) {
                MarkerSquareRenderer.draw(radius, color);
            }
            renderLabel(target.name, distance, radius, radius / MarkerSize.HALF_SIZE, focusProgress);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private static void faceCamera() {
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
    }

    private boolean renderFace(PlayerSnapshotMessage.PlayerPosition target, float radius) {
        ResourceLocation skin = PlayerFaceRenderer.skin(minecraft, target.id, target.name);
        if (skin == null) return false;
        MarkerSquareRenderer
            .drawOutline(radius * (1.0F + MarkerSquareRenderer.OUTLINE_RATIO), ModConfig.waypointBorderColor);
        PlayerFaceRenderer.drawWorld(minecraft, skin, radius);
        return true;
    }

    private void renderLabel(String name, double distance, float markerSize, float labelScale, float focusProgress) {
        String text = MarkerLabelRenderer.text(name, distance, focusProgress);
        if (text == null) return;
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(0.0F, markerSize + labelScale * (minecraft.fontRenderer.FONT_HEIGHT + 4.0F), 0.0F);
            GL11.glScalef(-labelScale, -labelScale, labelScale);
            int x = -minecraft.fontRenderer.getStringWidth(text) / 2;
            MarkerLabelRenderer.drawOutlined(minecraft.fontRenderer, text, x, 0);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private static void setupGl() {
        MarkerRenderState.setup();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
    }

    // Reads tan(half the render FOV) from the current projection matrix so the world
    // marker can be sized to the same FOV-independent pixels as the screen dots.
    private static void captureRenderFov() {
        FloatBuffer projection = ByteBuffer.allocateDirect(16 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        float m11 = projection.get(5);
        if (m11 != 0.0F) MarkerSize.renderTanHalfFov = 1.0F / m11;
    }

    private static void restoreGl() {
        MarkerRenderState.restore();
    }
}
