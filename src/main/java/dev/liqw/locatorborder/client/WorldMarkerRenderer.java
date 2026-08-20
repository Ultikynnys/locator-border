package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
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

    private static final double FOCUS_ALIGNMENT = Math.cos(Math.toRadians(3.0D));
    private static final double MARKER_HEIGHT = 1.0D;
    private static final float MARKER_RADIUS = 3.0F;
    private static final float LABEL_SCALE = 0.02F;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final ClientState state;

    WorldMarkerRenderer(ClientState state) {
        this.state = state;
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if (!ModConfig.enabled || minecraft.thePlayer == null
            || minecraft.theWorld == null
            || minecraft.gameSettings.hideGUI) return;

        PlayerSnapshotMessage.PlayerPosition aimed = aimedMarker(event.partialTicks);
        boolean playerListPressed = minecraft.gameSettings.keyBindPlayerList.getIsKeyPressed();
        setupGl();
        try {
            for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
                if (player.dimension != minecraft.thePlayer.dimension) continue;
                boolean focused = MarkerFocus.reveal(ModConfig.focusTrigger, player == aimed, playerListPressed);
                renderMarker(player, focused);
            }
        } finally {
            restoreGl();
        }
    }

    private PlayerSnapshotMessage.PlayerPosition aimedMarker(float partialTicks) {
        Vec3 eye = minecraft.renderViewEntity.getPosition(partialTicks);
        Vec3 look = minecraft.renderViewEntity.getLook(partialTicks);
        PlayerSnapshotMessage.PlayerPosition best = null;
        double bestAlignment = FOCUS_ALIGNMENT;
        double bestDistanceSquared = Double.POSITIVE_INFINITY;
        for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
            if (player.dimension != minecraft.thePlayer.dimension) continue;
            double dx = player.x - eye.xCoord;
            double dy = player.y + MARKER_HEIGHT - eye.yCoord;
            double dz = player.z - eye.zCoord;
            double alignment = MarkerFocus.alignment(look.xCoord, look.yCoord, look.zCoord, dx, dy, dz);
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            if (MarkerFocus.isFocused(alignment, FOCUS_ALIGNMENT)
                && MarkerFocus.isBetter(alignment, distanceSquared, bestAlignment, bestDistanceSquared)) {
                best = player;
                bestAlignment = alignment;
                bestDistanceSquared = distanceSquared;
            }
        }
        return best;
    }

    private void renderMarker(PlayerSnapshotMessage.PlayerPosition target, boolean focused) {
        double dx = target.x - minecraft.thePlayer.posX;
        double dy = target.y - minecraft.thePlayer.posY;
        double dz = target.z - minecraft.thePlayer.posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int color = MarkerColor.parse(ModConfig.markerColor);

        GL11.glPushMatrix();
        try {
            GL11.glTranslated(
                target.x - RenderManager.instance.viewerPosX,
                target.y + MARKER_HEIGHT - RenderManager.instance.viewerPosY,
                target.z - RenderManager.instance.viewerPosZ);
            faceCamera();
            float markerScale = Math.max(0.02F, (float) distance * 0.002F);
            GL11.glScalef(markerScale, markerScale, markerScale);
            float radius = focused ? MARKER_RADIUS * ModConfig.focusScale : MARKER_RADIUS;
            if (!ModConfig.playerFaces || !renderFace(target, radius)) drawDot(radius, color);
            renderLabel(target.name, (int) distance + "m", radius, focused);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private static void faceCamera() {
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
    }

    private boolean renderFace(PlayerSnapshotMessage.PlayerPosition target, float radius) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.95F);
        drawQuad(radius + 0.75F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        ResourceLocation skin = AbstractClientPlayer.getLocationSkin(target.name);
        AbstractClientPlayer.getDownloadImageSkin(skin, target.name);
        minecraft.getTextureManager()
            .bindTexture(skin);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.95F);
        float diameter = radius * 2.0F;
        drawFaceLayer(radius, diameter, 8.0F);
        drawFaceLayer(radius, diameter, 40.0F);
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

    private static void drawFaceLayer(float radius, float diameter, float textureX) {
        Tessellator tessellator = Tessellator.instance;
        float minU = textureX / 64.0F;
        float maxU = (textureX + 8.0F) / 64.0F;
        float minV = 8.0F / 64.0F;
        float maxV = 16.0F / 64.0F;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-radius, -radius, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(radius, -radius, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(radius, -radius + diameter, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(-radius, -radius + diameter, 0.0D, minU, minV);
        tessellator.draw();
    }

    private static void drawDot(float radius, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawCircle(radius + 0.75F, 0, 0.95F);
        drawCircle(radius, color, 0.9F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void drawCircle(float radius, int color, float alpha) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.addVertex(0.0D, 0.0D, 0.0D);
        for (int i = 0; i <= 16; i++) {
            double angle = Math.PI * 2.0D * i / 16.0D;
            tessellator.addVertex(Math.cos(angle) * radius, Math.sin(angle) * radius, 0.0D);
        }
        tessellator.draw();
    }

    private void renderLabel(String name, String distance, float radius, boolean focused) {
        String text = focused && ModConfig.displayDistance ? name + " " + distance : name;
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(0.0F, radius + 3.0F, 0.0F);
            GL11.glScalef(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
            int x = -minecraft.fontRenderer.getStringWidth(text) / 2;
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX != 0 || offsetY != 0) {
                        minecraft.fontRenderer.drawString(text, x + offsetX, offsetY, 0xFF000000);
                    }
                }
            }
            minecraft.fontRenderer.drawString(text, x, 0, 0xFFFFFFFF);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private static void setupGl() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
    }

    private static void restoreGl() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }
}
