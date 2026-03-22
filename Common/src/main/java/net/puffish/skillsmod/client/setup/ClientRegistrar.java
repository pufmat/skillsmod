package net.puffish.skillsmod.client.setup;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.client.network.ClientPacketHandler;
import net.puffish.skillsmod.network.InPacket;

import java.util.function.Function;

public interface ClientRegistrar {
	<T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ClientPacketHandler<T> handler);
	void registerOutPacket(Identifier id);
}

