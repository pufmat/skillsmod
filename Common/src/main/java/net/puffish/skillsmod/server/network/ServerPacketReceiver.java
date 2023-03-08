package net.puffish.skillsmod.server.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;

import java.util.function.Function;

public interface ServerPacketReceiver {
	<T extends InPacket> void registerPacket(Identifier identifier, Function<PacketByteBuf, T> reader, ServerPacketHandler<T> handler);
}
