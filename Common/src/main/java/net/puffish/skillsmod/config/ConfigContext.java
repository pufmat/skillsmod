package net.puffish.skillsmod.config;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;

public record ConfigContext(MinecraftServer server) {
	public DynamicRegistryManager dynamicRegistryManager() {
		return server.getRegistryManager();
	}
}
