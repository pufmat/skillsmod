package net.puffish.skillsmod.impl.config;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.api.config.ConfigContext;

import java.util.ArrayList;
import java.util.List;

public record ConfigContextImpl(MinecraftServer server, List<String> warnings) implements ConfigContext {
	public ConfigContextImpl(MinecraftServer server) {
		this(server, new ArrayList<>());
	}

	@Override
	public DynamicRegistryManager getDynamicRegistryManager() {
		return server.getRegistryManager();
	}

	@Override
	public ResourceManager getResourceManager() {
		return server.getResourceManager();
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public void addWarning(String failure) {
		warnings.add(failure);
	}
}
