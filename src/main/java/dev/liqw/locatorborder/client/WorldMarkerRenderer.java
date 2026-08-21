package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class WorldMarkerRenderer {

    private static final float MARKER_RADIUS = 3.0F;
    private static final float MINIMUM_MARKER_SCALE = 0.02F;
    private static final float MARKER_SCALE_PER_BLOCK = 0.002F;
    private static final double MARKER_SCALE_DISTANCE_THRESHOLD = 10.0D;

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

        PlayerSnapshotMessage.PlayerPosition aimed = aimedMarker(event.partialTicks);
        boolean playerListPressed = PlayerListFocus.isHeld(minecraft);
        setupGl();
        try {
            for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
                if (!MarkerGeometry.isInDimension(player.dimension, minecraft.thePlayer.dimension)) continue;
                boolean focused = MarkerFocus.reveal(ModConfig.focusTrigger, player == aimed, playerListPressed);
                float focusProgress = MarkerFocusState
                    .updateWorld(player.id, focused, ModConfig.animations, event.partialTicks);
                renderMarker(player, focusProgress);
            }
        } finally {
            restoreGl();
        }
    }

    private PlayerSnapshotMessage.PlayerPosition aimedMarker(float partialTicks) {
        Vec3 eye = minecraft.renderViewEntity.getPosition(partialTicks);
        Vec3 look = minecraft.renderViewEntity.getLook(partialTicks);
        PlayerSnapshotMessage.PlayerPosition best = null;
        double bestAlignment = MarkerFocus.AIM_ALIGNMENT;
        double bestDistanceSquared = Double.POSITIVE_INFINITY;
        for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
            if (!MarkerGeometry.isInDimension(player.dimension, minecraft.thePlayer.dimension)) continue;
            double dx = player.x - eye.xCoord;
            double dy = player.y + MarkerGeometry.HEIGHT - eye.yCoord;
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
        int color = MarkerColorResolver.resolve(minecraft, target);

        GL11.glPushMatrix();
        try {
            GL11.glTranslated(
                target.x - RenderManager.instance.viewerPosX,
                target.y + MarkerGeometry.HEIGHT - RenderManager.instance.viewerPosY,
                target.z - RenderManager.instance.viewerPosZ);
            faceCamera();
            float markerScale = markerScale(distance);
            float stateScale = MarkerSize.scale(focusProgress, ModConfig.waypointScale, ModConfig.focusScale);
            float radius = MARKER_RADIUS * stateScale;
            GL11.glPushMatrix();
            try {
                GL11.glScalef(markerScale, markerScale, markerScale);
                if (!ModConfig.playerFaces || !renderFace(target, radius)) {
                    MarkerDotRenderer.draw(radius, color, 0.75F, 0.95F, 0.9F);
                }
            } finally {
                GL11.glPopMatrix();
            }
            renderLabel(target.name, distance, radius * markerScale, markerScale * stateScale, focusProgress);
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
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.95F);
        drawQuad(radius + 0.75F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        PlayerFaceRenderer.drawWorld(minecraft, skin, radius);
        return true;
    }

    private static void drawQuad(float radius) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertex(-radius, -radius, 0.0D);
        tessellator.addVertex(radius, -radius, 0.0D);
        tessellator.addVertex(radius, radius, 0.0D);
        tessellator.addVertex(-radius, radius, 0.0D);
        tessellator.draw();
    }

    static float markerScale(double distance) {
        if (distance <= MARKER_SCALE_DISTANCE_THRESHOLD) return MINIMUM_MARKER_SCALE;
        return (float) distance * MARKER_SCALE_PER_BLOCK;
    }

    private void renderLabel(String name, double distance, float radius, float markerScale, float focusProgress) {
        String text = MarkerLabelRenderer
            .text(name, distance, focusProgress > 0.0F, ModConfig.displayPlayerName, ModConfig.displayDistance);
        if (text == null) return;
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(0.0F, radius + markerScale * 4.0F, 0.0F);
            GL11.glScalef(-markerScale, -markerScale, markerScale);
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

    private static void restoreGl() {
        MarkerRenderState.restore();
    }
}
