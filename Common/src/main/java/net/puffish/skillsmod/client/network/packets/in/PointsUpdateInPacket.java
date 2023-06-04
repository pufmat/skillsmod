package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.network.InPacket;

public class PointsUpdateInPacket implements InPacket {
	private final String categoryId;
	private final int points;
	private final boolean announceNewPoints;

	private PointsUpdateInPacket(String categoryId, int points, boolean announceNewPoints) {
		this.categoryId = categoryId;
		this.points = points;
		this.announceNewPoints = announceNewPoints;
	}

	public static PointsUpdateInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readString();
		var points = buf.readInt();
		var announceNewPoints = buf.readBoolean();

		return new PointsUpdateInPacket(
				categoryId,
				points,
				announceNewPoints
		);
	}

	public String getCategoryId() {
		return categoryId;
	}

	public int getPoints() {
		return points;
	}

	public boolean announceNewPoints() {
		return announceNewPoints;
	}
}
