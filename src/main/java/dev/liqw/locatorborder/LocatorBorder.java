package dev.liqw.locatorborder;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod(
    modid = LocatorBorder.MOD_ID,
    name = LocatorBorder.MOD_NAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]")
public final class LocatorBorder {

    public static final String MOD_ID = "locatorborder";
    public static final String MOD_NAME = "Locator Border";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    @SidedProxy(
        clientSide = "dev.liqw.locatorborder.client.ClientProxy",
        serverSide = "dev.liqw.locatorborder.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.load(event.getSuggestedConfigurationFile());
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @NetworkCheckHandler
    public boolean acceptRemoteVersions(Map<String, String> remoteVersions, Side remoteSide) {
        return true;
    }
}
