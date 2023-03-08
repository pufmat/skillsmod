package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.network.InPacket;

public class PointsUpdateInPacket implements InPacket {
	private final String categoryId;
	private final int points;

	private PointsUpdateInPacket(String categoryId, int points) {
		this.categoryId = categoryId;
		this.points = points;
	}

	public static PointsUpdateInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readString();
		var points = buf.readInt();

		return new PointsUpdateInPacket(
				categoryId,
				points
		);
	}

	public String getCategoryId() {
		return categoryId;
	}

	public int getPoints() {
		return points;
	}
}
