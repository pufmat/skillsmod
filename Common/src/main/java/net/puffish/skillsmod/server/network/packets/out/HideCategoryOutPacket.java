package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class HideCategoryOutPacket extends OutPacket {
	public HideCategoryOutPacket(String categoryId) {
		buf.writeString(categoryId);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.HIDE_CATEGORY;
	}
}
