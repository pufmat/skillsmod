package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class InvalidConfigOutPacket extends OutPacket {
	public static InvalidConfigOutPacket write() {
		return new InvalidConfigOutPacket();
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.INVALID_CONFIG;
	}
}
