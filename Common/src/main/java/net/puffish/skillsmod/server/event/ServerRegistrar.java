package net.puffish.skillsmod.server.event;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface ServerRegistrar {
	<V, T extends V> void register(Registry<V> registry, Identifier id, T entry);
}
