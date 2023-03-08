package net.puffish.skillsmod.server.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.network.InPacket;

public class SkillClickInPacket implements InPacket {
	private final String categoryId;
	private final String skillId;

	private SkillClickInPacket(String categoryId, String skillId) {
		this.categoryId = categoryId;
		this.skillId = skillId;
	}

	public static SkillClickInPacket read(PacketByteBuf buf) {
		return new SkillClickInPacket(
				buf.readString(),
				buf.readString()
		);
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getSkillId() {
		return skillId;
	}
}
