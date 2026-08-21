package net.puffish.skillsmod.server.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class BuyPointInPacket implements InPacket {
	private final Identifier categoryId;

	private BuyPointInPacket(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	public static BuyPointInPacket read(PacketByteBuf buf) {
		return new BuyPointInPacket(
				buf.readIdentifier()
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}
}
