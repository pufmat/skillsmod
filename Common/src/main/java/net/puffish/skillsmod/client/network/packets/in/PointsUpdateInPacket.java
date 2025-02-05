package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class PointsUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final int spentPoints;
	private final int earnedPoints;

	private PointsUpdateInPacket(Identifier categoryId, int spentPoints, int earnedPoints) {
		this.categoryId = categoryId;
		this.spentPoints = spentPoints;
		this.earnedPoints = earnedPoints;
	}

	public static PointsUpdateInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readIdentifier();
		var spentPoints = buf.readInt();
		var earnedPoints = buf.readInt();

		return new PointsUpdateInPacket(
				categoryId,
				spentPoints,
				earnedPoints
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public int getSpentPoints() {
		return spentPoints;
	}

	public int getEarnedPoints() {
		return earnedPoints;
	}
}
