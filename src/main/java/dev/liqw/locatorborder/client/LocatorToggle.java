package dev.liqw.locatorborder.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

public final class LocatorToggle {

    private static final String KEY_DESCRIPTION = "key.locatorborder.toggle";
    private static final String KEY_CATEGORY = "key.categories.locatorborder";

    private final KeyBinding binding = new KeyBinding(KEY_DESCRIPTION, Keyboard.KEY_NONE, KEY_CATEGORY);
    private boolean enabled = true;

    public void register() {
        ClientRegistry.registerKeyBinding(binding);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        while (binding.isPressed()) toggle();
    }

    public boolean isEnabled() {
        return enabled;
    }

    void toggle() {
        enabled = !enabled;
    }
}
