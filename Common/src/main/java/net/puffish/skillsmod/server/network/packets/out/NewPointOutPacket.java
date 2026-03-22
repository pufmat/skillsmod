package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public record NewPointOutPacket(Identifier categoryId) implements OutPacket {
	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeIdentifier(categoryId);
	}

	@Override
	public Identifier getId() {
		return Packets.NEW_POINT;
	}
}
