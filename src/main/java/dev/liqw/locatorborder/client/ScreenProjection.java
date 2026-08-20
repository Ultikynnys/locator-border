package dev.liqw.locatorborder.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Mouse;

import dev.liqw.locatorborder.ModConfig;

final class ScreenProjection {

    private static final float HOTBAR_WIDTH = 206.0F;
    private static final float HOTBAR_FADE_BUFFER = 48.0F;
    private static final Map<UUID, Float> ANIMATION = new HashMap<UUID, Float>();

    private final Minecraft minecraft;
    private final int width;
    private final int height;

    ScreenProjection(Minecraft minecraft, int width, int height) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
    }

    State compute(UUID id, float angle, float iconWidth, float iconHeight, float partialTicks) {
        double radians = Math.toRadians(angle);
        float directionX = (float) Math.sin(radians);
        float directionY = (float) -Math.cos(radians);
        Point outer = project(directionX, directionY, ModConfig.inset);

        boolean focused;
        if ("HOVER".equals(ModConfig.focusTrigger)) {
            int mouseX = Mouse.getX() * width / minecraft.displayWidth;
            int mouseY = height - Mouse.getY() * height / minecraft.displayHeight - 1;
            focused = mouseX >= outer.x - iconWidth / 2.0F && mouseX <= outer.x + iconWidth / 2.0F
                && mouseY >= outer.y - iconHeight / 2.0F
                && mouseY <= outer.y + iconHeight / 2.0F;
        } else if ("FOCAL".equals(ModConfig.focusTrigger)) {
            focused = Math.abs(angle) < 15.0F;
        } else if ("PLAYER_LIST".equals(ModConfig.focusTrigger)) {
            focused = minecraft.gameSettings.keyBindPlayerList.getIsKeyPressed();
        } else {
            focused = false;
        }

        float progress = ANIMATION.containsKey(id) ? ANIMATION.get(id) : 0.0F;
        if (ModConfig.animations) {
            float step = partialTicks / 5.0F;
            progress = focused ? Math.min(progress + step, 1.0F) : Math.max(progress - step, 0.0F);
        } else {
            progress = focused ? 1.0F : 0.0F;
        }
        ANIMATION.put(id, progress);

        float eased = progress * progress * (3.0F - 2.0F * progress);
        Point point = project(directionX, directionY, ModConfig.inset + (int) (ModConfig.focusInset * eased));
        float alpha = alpha(point.x, point.y);
        return new State(point.x, point.y, directionX, directionY, width / 2.0F, height / 2.0F, alpha, eased);
    }

    State computeStatic(float angle) {
        double radians = Math.toRadians(angle);
        float directionX = (float) Math.sin(radians);
        float directionY = (float) -Math.cos(radians);
        Point point = project(directionX, directionY, ModConfig.inset);
        return new State(
            point.x,
            point.y,
            directionX,
            directionY,
            width / 2.0F,
            height / 2.0F,
            alpha(point.x, point.y),
            0.0F);
    }

    private Point project(float dx, float dy, int margin) {
        float cx = width / 2.0F;
        float cy = height / 2.0F;
        float bx = Math.max(1.0F, cx - margin);
        float by = Math.max(1.0F, cy - margin);
        float scaleX = Math.abs(dx / Math.max(0.0001F, bx / cx));
        float scaleY = Math.abs(dy / Math.max(0.0001F, by / cy));
        float hitScale = Math.max(0.0001F, Math.max(scaleX, scaleY));
        return new Point(cx + dx / hitScale * bx, cy + dy / hitScale * by);
    }

    private float alpha(float x, float y) {
        if (y <= height / 2.0F) return 1.0F;
        float distance = Math.abs(x - width / 2.0F) - HOTBAR_WIDTH / 2.0F;
        if (!ModConfig.animations) return distance > 0.0F ? 1.0F : 0.0F;
        return clamp(distance / HOTBAR_FADE_BUFFER);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static final class Point {

        final float x;
        final float y;

        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    static final class State {

        final float x;
        final float y;
        final float directionX;
        final float directionY;
        final float centerX;
        final float centerY;
        final float alpha;
        final float focus;

        State(float x, float y, float directionX, float directionY, float centerX, float centerY, float alpha,
            float focus) {
            this.x = x;
            this.y = y;
            this.directionX = directionX;
            this.directionY = directionY;
            this.centerX = centerX;
            this.centerY = centerY;
            this.alpha = alpha;
            this.focus = focus;
        }

        int alpha(int color) {
            int source = color >>> 24;
            int result = (int) (source * alpha);
            return color & 0x00FFFFFF | result << 24;
        }

        int alpha(int color, float multiplier) {
            int source = color >>> 24;
            int result = (int) (source * alpha * Math.max(0.0F, Math.min(1.0F, multiplier)));
            return color & 0x00FFFFFF | result << 24;
        }
    }
}
