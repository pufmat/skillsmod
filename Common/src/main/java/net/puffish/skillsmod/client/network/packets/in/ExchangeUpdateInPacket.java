package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class ExchangeUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final int currentLevel;
	private final int currentCost;

	private ExchangeUpdateInPacket(Identifier categoryId, int currentLevel, int currentCost) {
		this.categoryId = categoryId;
		this.currentLevel = currentLevel;
		this.currentCost = currentCost;
	}

	public static ExchangeUpdateInPacket read(FriendlyByteBuf buf) {
		var categoryId = buf.readIdentifier();
		var currentLevel = buf.readInt();
		var currentCost = buf.readInt();

		return new ExchangeUpdateInPacket(
				categoryId,
				currentLevel,
				currentCost
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public int getCurrentCost() {
		return currentCost;
	}
}
