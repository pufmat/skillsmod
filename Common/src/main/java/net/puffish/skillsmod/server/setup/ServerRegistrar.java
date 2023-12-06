package net.puffish.skillsmod.server.setup;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public interface ServerRegistrar {
	<V, T extends V> void register(Registry<V> registry, Identifier id, T entry);
	<T extends GameRules.Rule<T>> void registerGameRule(GameRules.Key<T> key, GameRules.Type<T> type);
	<A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentSerializer<A, T> serializer);
}
