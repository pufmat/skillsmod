package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public class SkillUnlockOutPacket extends OutPacket {
	public SkillUnlockOutPacket(String categoryId, String skillId) {
		buf.writeString(categoryId);
		buf.writeString(skillId);
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SKILL_UNLOCK_PACKET;
	}
}
