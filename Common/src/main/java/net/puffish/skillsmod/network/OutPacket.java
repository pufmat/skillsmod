package net.puffish.skillsmod.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public abstract class OutPacket {
	protected final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

	public abstract Identifier getIdentifier();

	public PacketByteBuf getBuf() {
		return buf;
	}
}
