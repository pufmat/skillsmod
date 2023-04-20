package net.puffish.skillsmod.rewards;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Reward {
	void update(ServerPlayerEntity player, int count, boolean recent);

	void dispose(MinecraftServer server);
}
