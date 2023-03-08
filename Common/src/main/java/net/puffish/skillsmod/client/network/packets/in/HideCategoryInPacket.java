package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.network.InPacket;

public class HideCategoryInPacket implements InPacket {
	private final String categoryId;

	private HideCategoryInPacket(String categoryId) {
		this.categoryId = categoryId;
	}

	public static HideCategoryInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readString();

		return new HideCategoryInPacket(
				categoryId
		);
	}

	public String getCategoryId() {
		return categoryId;
	}
}
