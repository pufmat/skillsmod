package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class HideCategoryInPacket implements InPacket {
	private final Identifier categoryId;

	private HideCategoryInPacket(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	public static HideCategoryInPacket read(FriendlyByteBuf buf) {
		var categoryId = buf.readIdentifier();

		return new HideCategoryInPacket(
				categoryId
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}
}
