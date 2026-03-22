package net.puffish.skillsmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public interface OutPacket {
	Identifier getId();

	void write(RegistryFriendlyByteBuf buf);
}
