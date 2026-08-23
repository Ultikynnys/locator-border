package dev.liqw.locatorborder.client;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import dev.liqw.locatorborder.ModConfig;

final class MarkerSquareRenderer {

    // Waypoint visuals shared by world and screen-edge markers so both render
    // identically: the outline is a fraction of the half-size and both layers
    // use the same alphas.
    static final float OUTLINE_RATIO = 0.25F;
    private static final float OUTLINE_ALPHA = 0.95F;
    private static final float FILL_ALPHA = 0.9F;

    private MarkerSquareRenderer() {}

    // Draws a square waypoint with a configurable outline, shared by world and
    // screen-edge markers so both render the same shape.
    static void draw(float halfSize, int color) {
        drawOutline(halfSize * (1.0F + OUTLINE_RATIO), ModConfig.waypointBorderColor);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawQuad(halfSize, color, FILL_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    // Draws only the outline square, used as the background behind a face.
    static void drawOutline(float halfSize, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawQuad(halfSize, color, OUTLINE_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void drawQuad(float halfSize, int color, float alpha) {
        GL11.glColor4f(MarkerColor.red(color), MarkerColor.green(color), MarkerColor.blue(color), alpha);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertex(-halfSize, -halfSize, 0.0D);
        tessellator.addVertex(halfSize, -halfSize, 0.0D);
        tessellator.addVertex(halfSize, halfSize, 0.0D);
        tessellator.addVertex(-halfSize, halfSize, 0.0D);
        tessellator.draw();
    }
}
