package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class ExperienceUpdateOutPacket extends OutPacket {
	public static ExperienceUpdateOutPacket write(Identifier categoryId, int currentLevel, int currentExperience, int requiredExperience) {
		var packet = new ExperienceUpdateOutPacket();
		write(packet.buf, categoryId, currentLevel, currentExperience, requiredExperience);
		return packet;
	}

	public static void write(PacketByteBuf buf, Identifier categoryId, int currentLevel, int currentExperience, int requiredExperience) {
		buf.writeIdentifier(categoryId);
		buf.writeInt(currentLevel);
		buf.writeInt(currentExperience);
		buf.writeInt(requiredExperience);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.EXPERIENCE_UPDATE;
	}
}
