package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.network.InPacket;

public class SkillUnlockInPacket implements InPacket {
	private final String categoryId;
	private final String skillId;

	private SkillUnlockInPacket(String categoryId, String skillId) {
		this.categoryId = categoryId;
		this.skillId = skillId;
	}

	public static SkillUnlockInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readString();
		var skillId = buf.readString();
		return new SkillUnlockInPacket(
				categoryId,
				skillId
		);
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getSkillId() {
		return skillId;
	}
}
