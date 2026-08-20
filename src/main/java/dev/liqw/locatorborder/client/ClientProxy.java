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
        FMLCommonHandler.instance()
            .bus()
            .register(state);
        FMLCommonHandler.instance()
            .bus()
            .register(new ConfigChangeHandler());
        MinecraftForge.EVENT_BUS.register(new HudRenderer(state));
    }

    @Override
    public void handlePlayerSnapshot(PlayerSnapshotMessage message) {
        ClientState.receive(message);
    }

}
