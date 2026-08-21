package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Exchange {

	/// Returns the current level.
	int getLevel(ServerPlayerEntity player);

	/// Sets level.
	void setLevel(ServerPlayerEntity player, int level);

	/// Returns the cost at the specified level.
	int getCost(int level);

}
