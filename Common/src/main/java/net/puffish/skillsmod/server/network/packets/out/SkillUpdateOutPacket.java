package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public record SkillUpdateOutPacket(Identifier categoryId, String skillId, boolean unlocked) implements OutPacket {
	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeIdentifier(categoryId);
		buf.writeUtf(skillId);
		buf.writeBoolean(unlocked);
	}

	@Override
	public Identifier getId() {
		return Packets.SKILL_UPDATE;
	}
}
