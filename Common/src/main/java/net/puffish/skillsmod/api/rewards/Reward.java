package net.puffish.skillsmod.api.rewards;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Reward {
	void update(ServerPlayerEntity player, RewardContext context);

	void dispose(MinecraftServer server);
}
