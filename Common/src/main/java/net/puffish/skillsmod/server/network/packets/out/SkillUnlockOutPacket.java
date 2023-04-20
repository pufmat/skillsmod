package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class SkillUnlockOutPacket extends OutPacket {
	public static SkillUnlockOutPacket write(String categoryId, String skillId) {
		var packet = new SkillUnlockOutPacket();
		write(packet.buf, categoryId, skillId);
		return packet;
	}

	public static void write(PacketByteBuf buf, String categoryId, String skillId) {
		buf.writeString(categoryId);
		buf.writeString(skillId);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SKILL_UNLOCK_PACKET;
	}
}
