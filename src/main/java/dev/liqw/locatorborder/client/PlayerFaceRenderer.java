package dev.liqw.locatorborder.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

final class PlayerFaceRenderer {

    // 1.7.10 player skins are 64x32 (ImageBufferDownload always outputs a 64x32
    // image), so U spans 64 columns but V spans only 32 rows.
    private static final float TEXTURE_WIDTH = 64.0F;
    private static final float TEXTURE_HEIGHT = 32.0F;
    private static final float FACE_SIZE = 8.0F;
    private static final float FACE_V = 8.0F;
    private static final float BASE_FACE_U = 8.0F;
    private static final float HAT_FACE_U = 40.0F;

    private PlayerFaceRenderer() {}

    static ResourceLocation skin(Minecraft minecraft, UUID id, String name) {
        EntityPlayer player = minecraft.theWorld.func_152378_a(id);
        if (player instanceof AbstractClientPlayer) return ((AbstractClientPlayer) player).getLocationSkin();
        if (name == null || name.length() == 0) return null;
        ResourceLocation skin = AbstractClientPlayer.getLocationSkin(name);
        AbstractClientPlayer.getDownloadImageSkin(skin, name);
        return skin;
    }

    static void drawScreen(Minecraft minecraft, ResourceLocation skin, int size) {
        minecraft.getTextureManager()
            .bindTexture(skin);
        resetTextureColor();
        drawScreenLayer(size, BASE_FACE_U);
        drawScreenLayer(size, HAT_FACE_U);
    }

    static void drawWorld(Minecraft minecraft, ResourceLocation skin, float radius) {
        minecraft.getTextureManager()
            .bindTexture(skin);
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
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT);
    }

    private static void drawWorldLayer(float radius, float textureX) {
        Tessellator tessellator = Tessellator.instance;
        float minU = textureX / TEXTURE_WIDTH;
        float maxU = (textureX + FACE_SIZE) / TEXTURE_WIDTH;
        float minV = FACE_V / TEXTURE_HEIGHT;
        float maxV = (FACE_V + FACE_SIZE) / TEXTURE_HEIGHT;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-radius, -radius, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(radius, -radius, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(radius, radius, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(-radius, radius, 0.0D, minU, minV);
        tessellator.draw();
    }
}
