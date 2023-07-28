package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class SkillUnlockInPacket implements InPacket {
	private final Identifier categoryId;
	private final String skillId;

	private SkillUnlockInPacket(Identifier categoryId, String skillId) {
		this.categoryId = categoryId;
		this.skillId = skillId;
	}

	public static SkillUnlockInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readIdentifier();
		var skillId = buf.readString();
		return new SkillUnlockInPacket(
				categoryId,
				skillId
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public String getSkillId() {
		return skillId;
	}
}
