package dev.liqw.locatorborder.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import dev.liqw.locatorborder.LocatorBorder;
import dev.liqw.locatorborder.ModConfig;

public final class LocatorBorderConfigGui extends GuiConfig {

    public LocatorBorderConfigGui(GuiScreen parent) {
        super(
            parent,
            elements(),
            LocatorBorder.MOD_ID,
            false,
            false,
            GuiConfig.getAbridgedConfigPath(ModConfig.getConfigPath()));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<IConfigElement> elements() {
        Configuration configuration = ModConfig.getConfiguration();
        List<IConfigElement> elements = new ArrayList<IConfigElement>();
        for (String category : ModConfig.CLIENT_CATEGORIES) {
            elements.add(new ConfigElement(configuration.getCategory(category)));
        }
        return elements;
    }
}
