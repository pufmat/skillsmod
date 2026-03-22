package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public record ExperienceUpdateOutPacket(Identifier categoryId, int currentLevel, int currentExperience, int requiredExperience) implements OutPacket {
	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeIdentifier(categoryId);
		buf.writeInt(currentLevel);
		buf.writeInt(currentExperience);
		buf.writeInt(requiredExperience);
	}

	@Override
	public Identifier getId() {
		return Packets.EXPERIENCE_UPDATE;
	}
}
