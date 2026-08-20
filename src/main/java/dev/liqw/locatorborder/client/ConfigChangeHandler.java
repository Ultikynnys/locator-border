package dev.liqw.locatorborder.client;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.liqw.locatorborder.LocatorBorder;
import dev.liqw.locatorborder.ModConfig;

public final class ConfigChangeHandler {

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!LocatorBorder.MOD_ID.equals(event.modID)) return;
        ModConfig.getConfiguration()
            .save();
        ModConfig.reload();
        LocatorBorder.LOG.info("Saved and reloaded Locator Border client configuration.");
    }
}
