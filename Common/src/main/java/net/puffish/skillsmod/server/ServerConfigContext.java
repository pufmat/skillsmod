package net.puffish.skillsmod.server;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.utils.failure.Failure;

import java.util.ArrayList;
import java.util.List;

public record ServerConfigContext(MinecraftServer server, List<Failure> warnings) implements ConfigContext {
	public ServerConfigContext(MinecraftServer server) {
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
	public void addWarning(Failure failure) {
		warnings.add(failure);
	}
}
