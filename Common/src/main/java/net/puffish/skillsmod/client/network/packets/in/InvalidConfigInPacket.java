package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.network.InPacket;

public class InvalidConfigInPacket implements InPacket {
	private InvalidConfigInPacket() {

	}

	public static InvalidConfigInPacket read(PacketByteBuf buf) {
		return new InvalidConfigInPacket();
	}
}
