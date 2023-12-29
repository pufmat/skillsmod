package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class PointsUpdateOutPacket extends OutPacket {
	public static PointsUpdateOutPacket write(Identifier categoryId, int spentPoints, int earnedPoints, boolean announceNewPoints) {
		var packet = new PointsUpdateOutPacket();
		write(packet.buf, categoryId, spentPoints, earnedPoints, announceNewPoints);
		return packet;
	}

	public static void write(PacketByteBuf buf, Identifier categoryId, int spentPoints, int earnedPoints, boolean announceNewPoints) {
		buf.writeIdentifier(categoryId);
		buf.writeInt(spentPoints);
		buf.writeInt(earnedPoints);
		buf.writeBoolean(announceNewPoints);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.POINTS_UPDATE;
	}
}
