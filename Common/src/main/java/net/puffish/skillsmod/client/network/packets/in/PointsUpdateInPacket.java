package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class PointsUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final int spentPoints;
	private final int earnedPoints;
	private final boolean announceNewPoints;

	private PointsUpdateInPacket(Identifier categoryId, int spentPoints, int earnedPoints, boolean announceNewPoints) {
		this.categoryId = categoryId;
		this.spentPoints = spentPoints;
		this.earnedPoints = earnedPoints;
		this.announceNewPoints = announceNewPoints;
	}

	public static PointsUpdateInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readIdentifier();
		var spentPoints = buf.readInt();
		var earnedPoints = buf.readInt();
		var announceNewPoints = buf.readBoolean();

		return new PointsUpdateInPacket(
				categoryId,
				spentPoints,
				earnedPoints,
				announceNewPoints
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

	public boolean announceNewPoints() {
		return announceNewPoints;
	}
}
