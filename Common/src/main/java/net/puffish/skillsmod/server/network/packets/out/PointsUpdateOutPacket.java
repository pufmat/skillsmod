package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class PointsUpdateOutPacket extends OutPacket {
	public static PointsUpdateOutPacket write(String categoryId, int points, boolean announceNewPoints) {
		var packet = new PointsUpdateOutPacket();
		write(packet.buf, categoryId, points, announceNewPoints);
		return packet;
	}

	public static void write(PacketByteBuf buf, String categoryId, int points, boolean announceNewPoints) {
		buf.writeString(categoryId);
		buf.writeInt(points);
		buf.writeBoolean(announceNewPoints);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.POINTS_UPDATE_PACKET;
	}
}
