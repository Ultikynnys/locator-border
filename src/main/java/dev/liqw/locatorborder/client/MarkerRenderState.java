package dev.liqw.locatorborder.client;

import org.lwjgl.opengl.GL11;

final class MarkerRenderState {

    private MarkerRenderState() {}

    static void setup() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        // The waypoint quads are drawn with screen/world-facing winding; keep face
        // culling off so the square is never culled away, and stop writing depth so
        // markers never leave traces in the depth buffer.
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
    }

    static void restore() {
        resetColor();
        GL11.glPopAttrib();
    }

    static void resetColor() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
