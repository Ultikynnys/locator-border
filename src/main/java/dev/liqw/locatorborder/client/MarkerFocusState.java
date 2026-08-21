package dev.liqw.locatorborder.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class MarkerFocusState {

    private static final Map<UUID, Float> WORLD_PROGRESS = new HashMap<UUID, Float>();
    private static final Map<UUID, Float> SCREEN_PROGRESS = new HashMap<UUID, Float>();

    private MarkerFocusState() {}

    static float updateWorld(UUID id, boolean focused, boolean animated, float partialTicks) {
        return update(WORLD_PROGRESS, id, focused, animated, partialTicks);
    }

    static float updateScreen(UUID id, boolean focused, boolean animated, float partialTicks) {
        return update(SCREEN_PROGRESS, id, focused, animated, partialTicks);
    }

    private static float update(Map<UUID, Float> progressByPlayer, UUID id, boolean focused, boolean animated,
        float partialTicks) {
        float progress = progressByPlayer.containsKey(id) ? progressByPlayer.get(id) : 0.0F;
        if (animated) {
            float step = Math.max(0.0F, partialTicks) / 5.0F;
            progress = focused ? Math.min(progress + step, 1.0F) : Math.max(progress - step, 0.0F);
        } else {
            progress = focused ? 1.0F : 0.0F;
        }
        progressByPlayer.put(id, progress);
        return ease(progress);
    }

    static float ease(float progress) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    static void clear() {
        WORLD_PROGRESS.clear();
        SCREEN_PROGRESS.clear();
    }
}
