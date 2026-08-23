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

    // Draws a hollow square border on top of a face so the outline is always
    // visible regardless of how the face texture is rendered. Shared by the world
    // and screen-edge renderers so the face outline looks identical.
    static void drawBorder(float halfSize, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float thickness = Math.max(1.0F, halfSize * OUTLINE_RATIO);
        GL11.glColor4f(MarkerColor.red(color), MarkerColor.green(color), MarkerColor.blue(color), 1.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        float h = halfSize;
        float t = thickness;
        // top
        tessellator.addVertex(-h, -h, 0.0D);
        tessellator.addVertex(h, -h, 0.0D);
        tessellator.addVertex(h, -h + t, 0.0D);
        tessellator.addVertex(-h, -h + t, 0.0D);
        // bottom
        tessellator.addVertex(-h, h - t, 0.0D);
        tessellator.addVertex(h, h - t, 0.0D);
        tessellator.addVertex(h, h, 0.0D);
        tessellator.addVertex(-h, h, 0.0D);
        // left
        tessellator.addVertex(-h, -h + t, 0.0D);
        tessellator.addVertex(-h + t, -h + t, 0.0D);
        tessellator.addVertex(-h + t, h - t, 0.0D);
        tessellator.addVertex(-h, h - t, 0.0D);
        // right
        tessellator.addVertex(h - t, -h + t, 0.0D);
        tessellator.addVertex(h, -h + t, 0.0D);
        tessellator.addVertex(h, h - t, 0.0D);
        tessellator.addVertex(h - t, h - t, 0.0D);
        tessellator.draw();
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
