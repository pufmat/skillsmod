package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class ExperienceUpdateOutPacket extends OutPacket {
	public ExperienceUpdateOutPacket(String categoryId, float experienceProgress) {
		buf.writeString(categoryId);
		buf.writeFloat(experienceProgress);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.EXPERIENCE_UPDATE_PACKET;
	}
}
