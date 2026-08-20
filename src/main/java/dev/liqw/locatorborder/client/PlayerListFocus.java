package dev.liqw.locatorborder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

final class PlayerListFocus {

    private PlayerListFocus() {}

    static boolean isHeld(Minecraft minecraft) {
        KeyBinding binding = minecraft.gameSettings.keyBindPlayerList;
        int keyCode = binding.getKeyCode();
        return keyCode > Keyboard.KEY_NONE && Keyboard.isKeyDown(keyCode);
    }
}
