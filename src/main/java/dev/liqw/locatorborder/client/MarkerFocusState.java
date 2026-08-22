package dev.liqw.locatorborder.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class MarkerFocusState {

    private static final Map<UUID, Float> WORLD_PROGRESS = new HashMap<UUID, Float>();
    private static final Map<UUID, Float> SCREEN_PROGRESS = new HashMap<UUID, Float>();

    private MarkerFocusState() {}

    static float updateWorld(UUID id, boolean focused) {
        return update(WORLD_PROGRESS, id, focused);
    }

    static float updateScreen(UUID id, boolean focused) {
        return update(SCREEN_PROGRESS, id, focused);
    }

    private static float update(Map<UUID, Float> progressByPlayer, UUID id, boolean focused) {
        float progress = focused ? 1.0F : 0.0F;
        progressByPlayer.put(id, progress);
        return progress;
    }

    static void clear() {
        WORLD_PROGRESS.clear();
        SCREEN_PROGRESS.clear();
    }
}
