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

    private PlayerFaceRenderer() {}

    static ResourceLocation skin(Minecraft minecraft, UUID id) {
        EntityPlayer player = minecraft.theWorld.func_152378_a(id);
        if (player instanceof AbstractClientPlayer) return ((AbstractClientPlayer) player).getLocationSkin();
        return AbstractClientPlayer.locationStevePng;
    }

    static void drawScreen(Minecraft minecraft, UUID id, int size) {
        minecraft.getTextureManager()
            .bindTexture(skin(minecraft, id));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.func_152125_a(-size / 2, -size / 2, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
        Gui.func_152125_a(-size / 2, -size / 2, 40.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
    }

    static void drawWorld(Minecraft minecraft, UUID id, float radius) {
        minecraft.getTextureManager()
            .bindTexture(skin(minecraft, id));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.95F);
        drawWorldLayer(radius, 8.0F);
        drawWorldLayer(radius, 40.0F);
    }

    private static void drawWorldLayer(float radius, float textureX) {
        Tessellator tessellator = Tessellator.instance;
        float minU = textureX / 64.0F;
        float maxU = (textureX + 8.0F) / 64.0F;
        float minV = 8.0F / 64.0F;
        float maxV = 16.0F / 64.0F;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-radius, -radius, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(radius, -radius, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(radius, radius, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(-radius, radius, 0.0D, minU, minV);
        tessellator.draw();
    }
}
