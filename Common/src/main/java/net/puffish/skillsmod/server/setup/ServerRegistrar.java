package net.puffish.skillsmod.server.setup;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.server.network.ServerPacketHandler;

import java.util.function.Function;

public interface ServerRegistrar {
	<V, T extends V> void register(Registry<V> registry, Identifier id, T entry);
	<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentTypeInfo<A, T> info);
	<T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ServerPacketHandler<T> handler);
	void registerOutPacket(Identifier id);
}
