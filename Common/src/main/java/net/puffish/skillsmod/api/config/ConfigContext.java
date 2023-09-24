package net.puffish.skillsmod.api.config;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.puffish.skillsmod.api.utils.failure.Failure;

public interface ConfigContext {

	MinecraftServer getServer();
	DynamicRegistryManager getDynamicRegistryManager();
	ResourceManager getResourceManager();

	void addWarning(Failure failure);
}
