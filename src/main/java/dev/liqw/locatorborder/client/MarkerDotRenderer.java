package dev.liqw.locatorborder.client;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

final class MarkerDotRenderer {

    private static final int SEGMENTS = 16;

    private MarkerDotRenderer() {}

    static void draw(float radius, int color, float outlineWidth, float outlineAlpha, float fillAlpha) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawCircle(radius + outlineWidth, 0, outlineAlpha);
        drawCircle(radius, color, fillAlpha);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void drawCircle(float radius, int color, float alpha) {
        GL11.glColor4f(MarkerColor.red(color), MarkerColor.green(color), MarkerColor.blue(color), alpha);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.addVertex(0.0D, 0.0D, 0.0D);
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = Math.PI * 2.0D * i / SEGMENTS;
            tessellator.addVertex(Math.cos(angle) * radius, Math.sin(angle) * radius, 0.0D);
        }
        tessellator.draw();
    }
}
