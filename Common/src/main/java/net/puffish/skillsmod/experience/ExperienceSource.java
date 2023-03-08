package net.puffish.skillsmod.experience;

import net.minecraft.server.MinecraftServer;

public interface ExperienceSource {
	void dispose(MinecraftServer server);
}
