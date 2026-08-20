package dev.liqw.locatorborder.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import dev.liqw.locatorborder.LocatorBorder;
import dev.liqw.locatorborder.server.PlayerTracker;
import io.netty.buffer.ByteBuf;

public final class HelloMessage implements IMessage {

    public static final int PROTOCOL_VERSION = 1;
    private int protocolVersion = PROTOCOL_VERSION;

    public HelloMessage() {}

    public HelloMessage(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        protocolVersion = buffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(protocolVersion);
    }

    public static final class Handler implements IMessageHandler<HelloMessage, IMessage> {

        @Override
        public IMessage onMessage(HelloMessage message, MessageContext context) {
            if (message.protocolVersion != PROTOCOL_VERSION) {
                LocatorBorder.LOG.warn(
                    "Rejected Locator Border protocol {} from {}; expected {}.",
                    message.protocolVersion,
                    context.getServerHandler().playerEntity.getCommandSenderName(),
                    PROTOCOL_VERSION);
                PlayerTracker.reject(context.getServerHandler().playerEntity.getUniqueID());
                return null;
            }
            if (PlayerTracker.accept(context.getServerHandler().playerEntity.getUniqueID())) {
                LocatorBorder.LOG.info(
                    "Accepted Locator Border client {} with protocol {}.",
                    context.getServerHandler().playerEntity.getCommandSenderName(),
                    PROTOCOL_VERSION);
            }
            return null;
        }
    }
}
