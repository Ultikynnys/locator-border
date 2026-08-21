package dev.liqw.locatorborder.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

final class PlayerFaceRenderer {

    private static final float TEXTURE_SIZE = 64.0F;
    private static final float FACE_SIZE = 8.0F;
    private static final float FACE_V = 8.0F;
    private static final float BASE_FACE_U = 8.0F;
    private static final float HAT_FACE_U = 40.0F;

    private PlayerFaceRenderer() {}

    static ResourceLocation skin(Minecraft minecraft, UUID id) {
        EntityPlayer player = minecraft.theWorld.func_152378_a(id);
        if (player instanceof AbstractClientPlayer) return ((AbstractClientPlayer) player).getLocationSkin();
        return AbstractClientPlayer.locationStevePng;
    }

    static void drawScreen(Minecraft minecraft, UUID id, int size) {
        minecraft.getTextureManager()
            .bindTexture(skin(minecraft, id));
        resetTextureColor();
        drawScreenLayer(size, BASE_FACE_U);
        drawScreenLayer(size, HAT_FACE_U);
    }

    static void drawWorld(Minecraft minecraft, UUID id, float radius) {
        minecraft.getTextureManager()
            .bindTexture(skin(minecraft, id));
        resetTextureColor();
        drawWorldLayer(radius, BASE_FACE_U);
        drawWorldLayer(radius, HAT_FACE_U);
    }

    private static void resetTextureColor() {
        MarkerRenderState.resetColor();
    }

    private static void drawScreenLayer(int size, float textureX) {
        Gui.func_152125_a(
            -size / 2,
            -size / 2,
            textureX,
            FACE_V,
            (int) FACE_SIZE,
            (int) FACE_SIZE,
            size,
            size,
            TEXTURE_SIZE,
            TEXTURE_SIZE);
    }

    private static void drawWorldLayer(float radius, float textureX) {
        Tessellator tessellator = Tessellator.instance;
        float minU = textureX / TEXTURE_SIZE;
        float maxU = (textureX + FACE_SIZE) / TEXTURE_SIZE;
        float minV = FACE_V / TEXTURE_SIZE;
        float maxV = (FACE_V + FACE_SIZE) / TEXTURE_SIZE;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-radius, -radius, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(radius, -radius, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(radius, radius, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(-radius, radius, 0.0D, minU, minV);
        tessellator.draw();
    }
}
