package net.puffish.skillsmod.server.setup;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.puffish.skillsmod.network.InPacket;
import net.puffish.skillsmod.server.network.ServerPacketHandler;

import java.util.function.Function;

public interface ServerRegistrar {
	<V, T extends V> void register(Registry<V> registry, Identifier id, T entry);
	<T extends GameRules.Rule<T>> void registerGameRule(GameRules.Key<T> key, GameRules.Type<T> type);
	<A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentSerializer<A, T> serializer);
	<T extends InPacket> void registerInPacket(Identifier id, Function<PacketByteBuf, T> reader, ServerPacketHandler<T> handler);
	void registerOutPacket(Identifier id);
}
