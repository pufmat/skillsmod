package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;

public class SkillUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final String skillId;
	private final boolean unlocked;

	private SkillUpdateInPacket(Identifier categoryId, String skillId, boolean unlocked) {
		this.categoryId = categoryId;
		this.skillId = skillId;
		this.unlocked = unlocked;
	}

	public static SkillUpdateInPacket read(PacketByteBuf buf) {
		var categoryId = buf.readIdentifier();
		var skillId = buf.readString();
		var unlocked = buf.readBoolean();
		return new SkillUpdateInPacket(
				categoryId,
				skillId,
				unlocked
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public String getSkillId() {
		return skillId;
	}

	public boolean isUnlocked() {
		return unlocked;
	}
}
