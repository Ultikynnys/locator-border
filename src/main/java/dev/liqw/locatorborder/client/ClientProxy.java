package dev.liqw.locatorborder.client;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import dev.liqw.locatorborder.CommonProxy;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;

public final class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();
        ClientState state = new ClientState();
        LocatorToggle locatorToggle = new LocatorToggle();
        locatorToggle.register();
        FMLCommonHandler.instance()
            .bus()
            .register(state);
        FMLCommonHandler.instance()
            .bus()
            .register(locatorToggle);
        FMLCommonHandler.instance()
            .bus()
            .register(new ConfigChangeHandler());
        MinecraftForge.EVENT_BUS.register(new WorldMarkerRenderer(state, locatorToggle));
        MinecraftForge.EVENT_BUS.register(new ScreenEdgeMarkerRenderer(state, locatorToggle));
    }

    @Override
    public void handlePlayerSnapshot(PlayerSnapshotMessage message) {
        ClientState.receive(message);
    }

}
