package net.puffish.skillsmod.client.network.packets.out;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public record SkillClickOutPacket(Identifier categoryId, String skillId) implements OutPacket {
	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeIdentifier(categoryId);
		buf.writeUtf(skillId);
	}

	@Override
	public Identifier getId() {
		return Packets.SKILL_CLICK;
	}
}
