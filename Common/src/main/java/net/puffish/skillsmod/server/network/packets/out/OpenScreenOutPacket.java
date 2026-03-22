package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

import java.util.Optional;

public record OpenScreenOutPacket(Optional<Identifier> category) implements OutPacket {
	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeOptional(category, FriendlyByteBuf::writeIdentifier);
	}

	@Override
	public Identifier getId() {
		return Packets.OPEN_SCREEN;
	}
}
