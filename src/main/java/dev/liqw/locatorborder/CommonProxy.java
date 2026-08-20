package dev.liqw.locatorborder;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import dev.liqw.locatorborder.network.HelloMessage;
import dev.liqw.locatorborder.network.PlayerSnapshotMessage;
import dev.liqw.locatorborder.server.PlayerTracker;

public class CommonProxy {

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("locatorborder");

    public void preInit() {
        NETWORK.registerMessage(HelloMessage.Handler.class, HelloMessage.class, 0, Side.SERVER);
        NETWORK.registerMessage(PlayerSnapshotHandler.class, PlayerSnapshotMessage.class, 1, Side.CLIENT);
    }

    public void init() {
        FMLCommonHandler.instance()
            .bus()
            .register(new PlayerTracker());
    }

    public void handlePlayerSnapshot(PlayerSnapshotMessage message) {
        LocatorBorder.LOG.warn("Ignored a player snapshot delivered on a non-client side.");
    }

    public static final class PlayerSnapshotHandler implements IMessageHandler<PlayerSnapshotMessage, IMessage> {

        @Override
        public IMessage onMessage(PlayerSnapshotMessage message, MessageContext context) {
            if (!message.isValid()) {
                LocatorBorder.LOG.warn("Discarded a malformed Locator Border snapshot.");
                return null;
            }
            LocatorBorder.proxy.handlePlayerSnapshot(message);
            return null;
        }
    }
}
