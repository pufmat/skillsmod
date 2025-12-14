package net.puffish.skillsmod.server.setup;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.server.network.ServerPacketHandler;

import java.util.function.Function;

public interface ServerRegistrar {
	<V, T extends V> void register(Registry<V> registry, Identifier id, T entry);
	<A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentSerializer<A, T> serializer);
	<T extends InPacket> void registerInPacket(Identifier id, Function<RegistryByteBuf, T> reader, ServerPacketHandler<T> handler);
	void registerOutPacket(Identifier id);
}
