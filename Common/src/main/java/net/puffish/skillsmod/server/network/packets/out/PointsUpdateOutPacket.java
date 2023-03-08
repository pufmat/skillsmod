package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class PointsUpdateOutPacket extends OutPacket {
	public PointsUpdateOutPacket(String categoryId, int points) {
		buf.writeString(categoryId);
		buf.writeInt(points);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.POINTS_UPDATE_PACKET;
	}
}
