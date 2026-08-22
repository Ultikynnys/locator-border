package dev.liqw.locatorborder.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public final class PlayerSnapshotMessage implements IMessage {

    private static final int ABSOLUTE_MAX_PLAYERS = 1024;
    private static final int MAX_NAME_BYTES = 64;

    private long sequence;
    private List<PlayerPosition> players = Collections.emptyList();
    private boolean valid = true;

    public PlayerSnapshotMessage() {}

    public PlayerSnapshotMessage(long sequence, List<PlayerPosition> players) {
        this.sequence = sequence;
        this.players = players;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        valid = false;
        if (buffer.readableBytes() < 12) return;
        sequence = buffer.readLong();
        int count = buffer.readInt();
        if (count < 0 || count > ABSOLUTE_MAX_PLAYERS) return;

        List<PlayerPosition> decoded = new ArrayList<PlayerPosition>(count);
        try {
            for (int i = 0; i < count; i++) {
                UUID id = new UUID(buffer.readLong(), buffer.readLong());
                String name = ByteBufUtils.readUTF8String(buffer);
                if (name.length() == 0 || name.getBytes("UTF-8").length > MAX_NAME_BYTES) return;
                int dimension = buffer.readInt();
                double x = buffer.readDouble();
                double y = buffer.readDouble();
                double z = buffer.readDouble();
                if (!finite(x) || !finite(y) || !finite(z)) return;
                int teamColor = buffer.readInt();
                boolean sameTeam = buffer.readBoolean();
                decoded.add(new PlayerPosition(id, name, dimension, x, y, z, teamColor, sameTeam));
            }
        } catch (Exception malformed) {
            return;
        }
        players = Collections.unmodifiableList(decoded);
        valid = true;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(sequence);
        buffer.writeInt(players.size());
        for (PlayerPosition player : players) {
            buffer.writeLong(player.id.getMostSignificantBits());
            buffer.writeLong(player.id.getLeastSignificantBits());
            ByteBufUtils.writeUTF8String(buffer, player.name);
            buffer.writeInt(player.dimension);
            buffer.writeDouble(player.x);
            buffer.writeDouble(player.y);
            buffer.writeDouble(player.z);
            buffer.writeInt(player.teamColor);
            buffer.writeBoolean(player.sameTeam);
        }
    }

    public long getSequence() {
        return sequence;
    }

    public List<PlayerPosition> getPlayers() {
        return players;
    }

    public boolean isValid() {
        return valid;
    }

    private static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public static final class PlayerPosition {

        public final UUID id;
        public final String name;
        public final int dimension;
        public final double x;
        public final double y;
        public final double z;
        public final int teamColor;
        public final boolean sameTeam;

        public PlayerPosition(UUID id, String name, int dimension, double x, double y, double z, int teamColor,
            boolean sameTeam) {
            this.id = id;
            this.name = name;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.teamColor = teamColor;
            this.sameTeam = sameTeam;
        }
    }

}
