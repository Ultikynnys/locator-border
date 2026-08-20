package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class ScreenEdgeMarkerRenderer extends Gui {

    private static final int DOT_SIZE = 7;
    private static final int FACE_SIZE = 8;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final ClientState state;
    private final LocatorToggle locatorToggle;

    ScreenEdgeMarkerRenderer(ClientState state, LocatorToggle locatorToggle) {
        this.state = state;
        this.locatorToggle = locatorToggle;
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !locatorToggle.isEnabled()
            || minecraft.thePlayer == null
            || minecraft.theWorld == null
            || minecraft.renderViewEntity == null
            || minecraft.gameSettings.hideGUI) return;

        ScaledResolution resolution = event.resolution;
        Vec3 eye = minecraft.renderViewEntity.getPosition(event.partialTicks);
        CameraBasis camera = cameraBasis(event.partialTicks);
        boolean playerListPressed = PlayerListFocus.isHeld(minecraft);
        setupGl();
        try {
            for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
                if (player.dimension != minecraft.thePlayer.dimension) continue;
                renderMarker(player, eye, camera, resolution, playerListPressed);
            }
        } finally {
            restoreGl();
        }
    }

    private void renderMarker(PlayerSnapshotMessage.PlayerPosition target, Vec3 eye, CameraBasis camera,
        ScaledResolution resolution, boolean playerListPressed) {
        double dx = target.x - eye.xCoord;
        double dy = target.y + 1.0D - eye.yCoord;
        double dz = target.z - eye.zCoord;
        ScreenEdgeProjection.Point point = ScreenEdgeProjection.project(
            dot(dx, dy, dz, camera.rightX, camera.rightY, camera.rightZ),
            dot(dx, dy, dz, camera.upX, camera.upY, camera.upZ),
            dot(dx, dy, dz, camera.forwardX, camera.forwardY, camera.forwardZ),
            minecraft.gameSettings.fovSetting,
            resolution.getScaledWidth(),
            resolution.getScaledHeight(),
            Math.max(ModConfig.inset, FACE_SIZE));
        if (point == null) return;

        boolean focused = MarkerFocus.reveal(ModConfig.focusTrigger, false, playerListPressed);
        float scale = focused ? ModConfig.focusScale : 1.0F;
        int size = Math.max(2, Math.round((ModConfig.playerFaces ? FACE_SIZE : DOT_SIZE) * scale));
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(point.x, point.y, 0.0F);
            if (ModConfig.playerFaces) renderFace(target.id, size);
            else renderDot(size, MarkerColor.parse(ModConfig.markerColor));
            renderLabel(target.name, (int) distance + "m", size, focused, point);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private void renderFace(java.util.UUID id, int size) {
        drawRect(-size / 2 - 1, -size / 2 - 1, (size + 1) / 2 + 1, (size + 1) / 2 + 1, 0xFF000000);
        PlayerFaceRenderer.drawScreen(minecraft, id, size);
    }

    private static void renderDot(int size, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawCircle(size * 0.5F + 1.0F, 0);
        drawCircle(size * 0.5F, color);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void drawCircle(float radius, int color) {
        GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.addVertex(0.0D, 0.0D, 0.0D);
        for (int i = 0; i <= 16; i++) {
            double angle = Math.PI * 2.0D * i / 16.0D;
            tessellator.addVertex(Math.cos(angle) * radius, Math.sin(angle) * radius, 0.0D);
        }
        tessellator.draw();
    }

    private void renderLabel(String name, String distance, int size, boolean focused,
        ScreenEdgeProjection.Point point) {
        String text = MarkerLabelRenderer.text(name, distance, focused, ModConfig.displayDistance);
        int width = minecraft.fontRenderer.getStringWidth(text);
        int x;
        int y;
        if (Math.abs(point.directionX) >= Math.abs(point.directionY)) {
            x = point.directionX < 0.0F ? size / 2 + 4 : -size / 2 - width - 4;
            y = -minecraft.fontRenderer.FONT_HEIGHT / 2;
        } else {
            x = -width / 2;
            y = point.directionY < 0.0F ? size / 2 + 3 : -size / 2 - minecraft.fontRenderer.FONT_HEIGHT - 3;
        }
        MarkerLabelRenderer.drawOutlined(minecraft.fontRenderer, text, x, y);
    }

    private CameraBasis cameraBasis(float partialTicks) {
        float yaw = interpolateRotation(
            minecraft.renderViewEntity.prevRotationYaw,
            minecraft.renderViewEntity.rotationYaw,
            partialTicks);
        float pitch = minecraft.renderViewEntity.prevRotationPitch
            + (minecraft.renderViewEntity.rotationPitch - minecraft.renderViewEntity.prevRotationPitch) * partialTicks;
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double sinYaw = Math.sin(yawRadians);
        double cosYaw = Math.cos(yawRadians);
        double sinPitch = Math.sin(pitchRadians);
        double cosPitch = Math.cos(pitchRadians);
        return new CameraBasis(
            -cosYaw,
            0.0D,
            -sinYaw,
            -sinYaw * sinPitch,
            cosPitch,
            cosYaw * sinPitch,
            -sinYaw * cosPitch,
            -sinPitch,
            cosYaw * cosPitch);
    }

    private static float interpolateRotation(float previous, float current, float partialTicks) {
        return previous + MathHelper.wrapAngleTo180_float(current - previous) * partialTicks;
    }

    private static double dot(double x, double y, double z, double basisX, double basisY, double basisZ) {
        return x * basisX + y * basisY + z * basisZ;
    }

    private static void setupGl() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    private static void restoreGl() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }

    private static final class CameraBasis {

        final double rightX;
        final double rightY;
        final double rightZ;
        final double upX;
        final double upY;
        final double upZ;
        final double forwardX;
        final double forwardY;
        final double forwardZ;

        CameraBasis(double rightX, double rightY, double rightZ, double upX, double upY, double upZ, double forwardX,
            double forwardY, double forwardZ) {
            this.rightX = rightX;
            this.rightY = rightY;
            this.rightZ = rightZ;
            this.upX = upX;
            this.upY = upY;
            this.upZ = upZ;
            this.forwardX = forwardX;
            this.forwardY = forwardY;
            this.forwardZ = forwardZ;
        }
    }
}
