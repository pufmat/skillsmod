package net.puffish.skillsmod.api.config;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;

public interface ConfigContext {

	MinecraftServer getServer();
	DynamicRegistryManager getDynamicRegistryManager();
	ResourceManager getResourceManager();

	void addWarning(String message);
}
