package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.input.Mouse;
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
        setupGl();
        try {
            for (PlayerSnapshotMessage.PlayerPosition player : state.get().players) {
                if (!MarkerGeometry.isInDimension(player.dimension, minecraft.thePlayer.dimension)) continue;
                if (!PlayerVisibility.shouldShow(minecraft, player)) continue;
                renderMarker(player, eye, camera, resolution);
            }
        } finally {
            restoreGl();
        }
    }

    private void renderMarker(PlayerSnapshotMessage.PlayerPosition target, Vec3 eye, CameraBasis camera,
        ScaledResolution resolution) {
        double dx = target.x - eye.xCoord;
        double dy = target.y + MarkerGeometry.HEIGHT - eye.yCoord;
        double dz = target.z - eye.zCoord;
        double cameraX = dot(dx, dy, dz, camera.rightX, camera.rightY, camera.rightZ);
        double cameraY = dot(dx, dy, dz, camera.upX, camera.upY, camera.upZ);
        double cameraZ = dot(dx, dy, dz, camera.forwardX, camera.forwardY, camera.forwardZ);
        int baseSize = ModConfig.playerFaces ? FACE_SIZE : DOT_SIZE;
        ScreenEdgeProjection.Point outerPoint = ScreenEdgeProjection.project(
            cameraX,
            cameraY,
            cameraZ,
            minecraft.gameSettings.fovSetting,
            resolution.getScaledWidth(),
            resolution.getScaledHeight(),
            baseSize);
        if (outerPoint == null) return;

        boolean focused = isAimed(outerPoint, baseSize, resolution);
        float focusProgress = MarkerFocusState.updateScreen(target.id, focused);
        ScreenEdgeProjection.Point point = ScreenEdgeProjection.project(
            cameraX,
            cameraY,
            cameraZ,
            minecraft.gameSettings.fovSetting,
            resolution.getScaledWidth(),
            resolution.getScaledHeight(),
            baseSize);
        float scale = MarkerSize.scale(focusProgress);
        int size = Math.max(2, Math.round(baseSize * scale));
        double distance = MarkerGeometry.distance(dx, dy, dz);
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(point.x, point.y, 0.0F);
            if (!ModConfig.playerFaces || !renderFace(target, size)) {
                MarkerDotRenderer.draw(size * 0.5F, MarkerColorResolver.resolve(target), 1.0F, 1.0F, 1.0F);
            }
            renderLabel(target.name, distance, size, scale, focusProgress, point);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private boolean renderFace(PlayerSnapshotMessage.PlayerPosition target, int size) {
        ResourceLocation skin = PlayerFaceRenderer.skin(minecraft, target.id, target.name);
        if (skin == null) return false;
        int outline = 0xFF000000;
        drawRect(-size / 2 - 1, -size / 2 - 1, (size + 1) / 2 + 1, (size + 1) / 2 + 1, outline);
        PlayerFaceRenderer.drawScreen(minecraft, skin, size);
        return true;
    }

    private void renderLabel(String name, double distance, int size, float scale, float focusProgress,
        ScreenEdgeProjection.Point point) {
        String text = MarkerLabelRenderer.text(name, distance, focusProgress);
        if (text == null) return;
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
        GL11.glPushMatrix();
        try {
            GL11.glScalef(scale, scale, 1.0F);
            MarkerLabelRenderer
                .drawOutlined(minecraft.fontRenderer, text, Math.round(x / scale), Math.round(y / scale));
        } finally {
            GL11.glPopMatrix();
        }
    }

    private boolean isAimed(ScreenEdgeProjection.Point point, int size, ScaledResolution resolution) {
        float mouseX = Mouse.getX() * resolution.getScaledWidth() / (float) minecraft.displayWidth;
        float mouseY = resolution.getScaledHeight()
            - Mouse.getY() * resolution.getScaledHeight() / (float) minecraft.displayHeight
            - 1.0F;
        return ScreenMarkerFocus.hover(mouseX, mouseY, point, size, size);
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
        MarkerRenderState.setup();
    }

    private static void restoreGl() {
        MarkerRenderState.restore();
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
