package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Experience {
	int getTotal(ServerPlayerEntity player);

	void setTotal(ServerPlayerEntity player, int amount);

	void addTotal(ServerPlayerEntity player, int amount);

	int getLevel(ServerPlayerEntity player);

	int getCurrent(ServerPlayerEntity player);

	int getRequired(ServerPlayerEntity player, int level);

	int getRequiredTotal(ServerPlayerEntity player, int level);
}
