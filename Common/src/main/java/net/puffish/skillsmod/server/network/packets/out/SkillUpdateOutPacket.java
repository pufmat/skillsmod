package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class SkillUpdateOutPacket extends OutPacket {
	public static SkillUpdateOutPacket write(Identifier categoryId, String skillId, boolean unlocked) {
		var packet = new SkillUpdateOutPacket();
		write(packet.buf, categoryId, skillId, unlocked);
		return packet;
	}

	public static void write(PacketByteBuf buf, Identifier categoryId, String skillId, boolean unlocked) {
		buf.writeIdentifier(categoryId);
		buf.writeString(skillId);
		buf.writeBoolean(unlocked);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SKILL_UPDATE;
	}
}
