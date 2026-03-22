package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class NewPointInPacket implements InPacket {
	private final Identifier categoryId;

	private NewPointInPacket(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	public static NewPointInPacket read(FriendlyByteBuf buf) {
		var categoryId = buf.readIdentifier();

		return new NewPointInPacket(
				categoryId
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}
}
