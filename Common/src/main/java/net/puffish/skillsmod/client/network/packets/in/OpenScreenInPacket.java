package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.InPacket;

import java.util.Optional;

public class OpenScreenInPacket implements InPacket {
	private final Optional<Identifier> categoryId;

	private OpenScreenInPacket(Optional<Identifier> categoryId) {
		this.categoryId = categoryId;
	}

	public static OpenScreenInPacket read(FriendlyByteBuf buf) {
		return new OpenScreenInPacket(buf.readOptional(FriendlyByteBuf::readIdentifier));
	}

	public Optional<Identifier> getCategoryId() {
		return categoryId;
	}
}
