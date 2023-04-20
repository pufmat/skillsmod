package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class ExperienceUpdateOutPacket extends OutPacket {
	public static ExperienceUpdateOutPacket write(String categoryId, float experienceProgress) {
		var packet = new ExperienceUpdateOutPacket();
		write(packet.buf, categoryId, experienceProgress);
		return packet;
	}

	public static void write(PacketByteBuf buf, String categoryId, float experienceProgress) {
		buf.writeString(categoryId);
		buf.writeFloat(experienceProgress);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.EXPERIENCE_UPDATE_PACKET;
	}
}
