package net.puffish.skillsmod.config;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.utils.failure.Failure;

import java.util.ArrayList;
import java.util.List;

public record ConfigContext(MinecraftServer server, List<Failure> warnings) {
	public ConfigContext(MinecraftServer server) {
		this(server, new ArrayList<>());
	}

	public DynamicRegistryManager dynamicRegistryManager() {
		return server.getRegistryManager();
	}

	public void addWarning(Failure failure) {
		warnings.add(failure);
	}
}
