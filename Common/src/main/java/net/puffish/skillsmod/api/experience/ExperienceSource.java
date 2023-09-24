package net.puffish.skillsmod.api.experience;

import net.minecraft.server.MinecraftServer;

public interface ExperienceSource {
	void dispose(MinecraftServer server);
}
