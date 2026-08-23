package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.liqw.locatorborder.LocatorBorder;
import dev.liqw.locatorborder.ModConfig;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class ScreenEdgeMarkerRenderer {

    private static final int EDGE_INSET = 4;
    private static boolean screenWaypointLogged;

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
            || !ModConfig.screenSpaceWaypoint
            || minecraft.thePlayer == null
            || minecraft.theWorld == null
            || minecraft.renderViewEntity == null
            || minecraft.gameSettings.hideGUI) return;

        ScaledResolution resolution = event.resolution;
        Vec3 eye = minecraft.renderViewEntity.getPosition(event.partialTicks);
        eye.yCoord += minecraft.renderViewEntity.getEyeHeight();
        CameraBasis camera = cameraBasis(event.partialTicks);
        setupGl();
        // Guarantee the 2D overlay projection is active so edge waypoints draw at
        // scaled screen coordinates instead of inheriting the world projection.
        minecraft.entityRenderer.setupOverlayRendering();
        try {
            for (PlayerSnapshotMessage.PlayerPosition player : state.waypoints(minecraft.thePlayer.dimension)) {
                if (!MarkerGeometry.isInDimension(player.dimension, minecraft.thePlayer.dimension)) continue;
                if (!PlayerVisibility.shouldShow(player)) continue;
                renderMarker(player, eye, camera, resolution);
            }
        } finally {
            restoreGl();
        }
    }

    private void renderMarker(PlayerSnapshotMessage.PlayerPosition target, Vec3 eye, CameraBasis camera,
        ScaledResolution resolution) {
        double dx = target.x - eye.xCoord;
        double dy = target.y + MarkerGeometry.MARKER_HEIGHT - eye.yCoord;
        double dz = target.z - eye.zCoord;
        double distance = MarkerGeometry.distance(dx, dy, dz);
        float fov = minecraft.gameSettings.fovSetting;
        int height = resolution.getScaledHeight();
        int width = resolution.getScaledWidth();
        ScreenEdgeProjection.Point outerPoint = ScreenEdgeProjection.project(
            dx,
            dy,
            dz,
            camera.forwardX,
            camera.forwardZ,
            camera.rightX,
            camera.rightZ,
            fov,
            width,
            height,
            EDGE_INSET);
        // The world-space dot is visible for this target, so the world dot wins and
        // no screen-edge dot is drawn. The world dot is always rendered separately.
        if (outerPoint == null) return;

        int aimDiameter = Math.round(MarkerSize.screenHalfSize(distance, 0.0F, fov, height) * 2.0F);
        boolean focused = isAimed(outerPoint, aimDiameter, resolution);
        float focusProgress = MarkerFocusState.updateScreen(target.id, focused);
        float halfSize = MarkerSize.screenHalfSize(distance, focusProgress, fov, height);
        int size = Math.round(halfSize * 2.0F);
        ScreenEdgeProjection.Point point = ScreenEdgeProjection.project(
            dx,
            dy,
            dz,
            camera.forwardX,
            camera.forwardZ,
            camera.rightX,
            camera.rightZ,
            fov,
            width,
            height,
            Math.max(EDGE_INSET, size / 2));
        if (!screenWaypointLogged) {
            screenWaypointLogged = true;
            LocatorBorder.LOG.info("Screen waypoint for {} drawn at screen ({}, {}).", target.name, point.x, point.y);
        }
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(point.x, point.y, 0.0F);
            if (!ModConfig.playerFaces || !renderFace(target, size)) {
                MarkerSquareRenderer.draw(size * 0.5F, MarkerColorResolver.resolve(target));
            }
            renderLabel(target.name, distance, size, halfSize / MarkerSize.HALF_SIZE, focusProgress, point);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private boolean renderFace(PlayerSnapshotMessage.PlayerPosition target, int size) {
        ResourceLocation skin = PlayerFaceRenderer.skin(minecraft, target.id, target.name);
        if (skin == null) return false;
        MarkerSquareRenderer
            .drawOutline(size / 2.0F * (1.0F + MarkerSquareRenderer.OUTLINE_RATIO), ModConfig.waypointBorderColor);
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
